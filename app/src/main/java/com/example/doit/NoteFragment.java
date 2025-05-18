package com.example.doit;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doit.databinding.FragmentNoteBinding;
import com.example.doit.entity.ContentEntity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NoteFragment extends Fragment {

    private static final String TAG = "NoteFragment";

    private NavController navController;
    private FragmentNoteBinding noteBinding;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String NOTE_REFERENCE_PATH;
    private String CONTENT_REFERENCE_PATH;

    public static final String BUNDLE_KEY = "note_bundle_key";

    private String[] noteNameAndPath;

    public NoteFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteNameAndPath = getArguments().getStringArray(BUNDLE_KEY);

            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            NOTE_REFERENCE_PATH = noteNameAndPath[1];
            CONTENT_REFERENCE_PATH = NOTE_REFERENCE_PATH + "/content/main";
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        noteBinding = FragmentNoteBinding.inflate(inflater, container, false);
        return noteBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        DocumentReference docRef = db.document(CONTENT_REFERENCE_PATH);
        docRef.get()
                .continueWithTask(task -> createContentDocumentIfNotExists(task, docRef)) // if content document doesn't exists creates and returns it
                                                                                // if exists returns it
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "Content loaded successfully");
                    String content = result.getString("content");
                    noteBinding.noteText.setText(content != null ? content : ""); // sets up content text
                    noteBinding.noteName.setText(noteNameAndPath[0]);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Content loading failed: ", e);
                    Toast.makeText(requireContext(), "Ошибка при загрузке: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        noteBinding.noteBackButton.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.action_noteFragment_to_noteListFragment));
    }

    @NonNull
    private Task<DocumentSnapshot> createContentDocumentIfNotExists(Task<DocumentSnapshot> task, DocumentReference docRef) throws Exception {
        if (!task.isSuccessful()) {
            throw task.getException();
        }

        DocumentSnapshot documentSnapshot = task.getResult();
        if (documentSnapshot != null && documentSnapshot.exists()) {
            return Tasks.forResult(documentSnapshot);
        } else {
            Log.d(TAG, "Content not found, creating default");
            return docRef.set(ContentEntity.createDefaultContent())
                    .continueWithTask(setTask -> {
                        if (!setTask.isSuccessful()) {
                            throw setTask.getException();
                        }
                        return docRef.get();
                    });
        }
    }
}