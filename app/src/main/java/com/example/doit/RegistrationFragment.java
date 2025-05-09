package com.example.doit;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.CredentialManager;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doit.databinding.FragmentLoginBinding;
import com.example.doit.databinding.FragmentRegistrationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class RegistrationFragment extends Fragment {

    private static final String TAG_EMAIL_PASSWORD = "EmailPasswordRegister";

    private FragmentRegistrationBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(binding.singUpLayout);

        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.fieldEmailLogin.getText().toString();
                String password = binding.fieldPasswordLogin.getText().toString();
                createAccount(email, password);
            }
        });

        binding.signUpOfferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_registrationFragment_to_loginFragment);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void createAccount(String email, String password) {
        Log.d(TAG_EMAIL_PASSWORD, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_EMAIL_PASSWORD, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_EMAIL_PASSWORD, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Ошибка регистрации: " + Objects.requireNonNull(task.getException()),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
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

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(125, VibrationEffect.DEFAULT_AMPLITUDE));

            Navigation.findNavController(binding.singUpLayout).navigate(R.id.action_registrationFragment_to_noteListFragment);
        } else {
            // invalid data
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}