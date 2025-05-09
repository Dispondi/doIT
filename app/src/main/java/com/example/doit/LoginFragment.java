package com.example.doit;

import static androidx.core.content.ContextCompat.getSystemService;

import androidx.credentials.CredentialManager;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.fragment.app.Fragment;

import android.os.CancellationSignal;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doit.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.credentials.Credential;
import androidx.credentials.CustomCredential;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
import java.util.concurrent.Executors;

import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {

    private static final String TAG_EMAIL_PASSWORD = "EmailPasswordLogin";
    private static final String TAG_GOOGLE = "GoogleLogin";

    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private NavController navController;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(requireContext());
        navController = Navigation.findNavController(binding.singInLayout);

        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.fieldEmailLogin.getText().toString();
                String password = binding.fieldPasswordLogin.getText().toString();
                signIn(email, password);
            }
        });

        binding.signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGoogleOptionDialog();
            }
        });

        binding.signUpOfferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_loginFragment_to_registrationFragment);
            }
        });
    }

    private void signIn(String email, String password) {
        Log.d(TAG_EMAIL_PASSWORD, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_EMAIL_PASSWORD, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_EMAIL_PASSWORD, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Ошибка входа: " + Objects.requireNonNull(task.getException()),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        } else showBottomSheet();
    }

    private void reload() {
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                } else {
                    Log.e(TAG_EMAIL_PASSWORD, "reload", task.getException());
                }
            }
        });
    }

    private void showBottomSheet() {
        // Create the bottom sheet configuration for the Credential Manager request
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(requireContext().getString(R.string.default_web_client_id))
                .build();

        // Create the Credential Manager request using the configuration created above
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        launchCredentialManager(request);
    }

    private void showGoogleOptionDialog() {
        GetSignInWithGoogleOption signInWithGoogleOption = new GetSignInWithGoogleOption
                .Builder(requireContext().getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build();

        launchCredentialManager(request);
    }

    private void launchCredentialManager(GetCredentialRequest request) {
        credentialManager.getCredentialAsync(
                requireContext(),
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Extract credential from the result returned by Credential Manager
                        createGoogleIdToken(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG_GOOGLE, "Couldn't retrieve user's credentials", e);
                    }
                }
        );
    }

    private void createGoogleIdToken(Credential credential) {
        // Check if credential is of type Google ID
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            // Create Google ID Token
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.w(TAG_GOOGLE, "Credential is not of type Google ID!");
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG_GOOGLE, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        Toast.makeText(requireContext(), "Ошибка входа в FirebaseAuth", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(125, VibrationEffect.DEFAULT_AMPLITUDE));


            navController.navigate(R.id.action_loginFragment_to_noteListFragment);
        } else {
            // invalid data
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = binding.fieldEmailLogin.getText().toString();
        if (TextUtils.isEmpty(email)) {
            binding.emailFieldLoginTextLayout.setError("Необходимо");
            valid = false;
        } else {
            binding.emailFieldLoginTextLayout.setError(null);
        }

        String password = binding.fieldPasswordLogin.getText().toString();
        if (TextUtils.isEmpty(password)) {
            binding.passwordFieldLoginTextLayout.setError("Необходимо");
            valid = false;
        } else {
            binding.passwordFieldLoginTextLayout.setError(null);
        }

        return valid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}