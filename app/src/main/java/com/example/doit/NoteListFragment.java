package com.example.doit;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doit.databinding.FragmentNoteListBinding;
import com.example.doit.entity.NoteEntity;
import com.example.doit.entity.UserEntity;
import com.example.doit.recyclerview.NotesAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";

    private FragmentNoteListBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration notesListener;

    private String userUID;
    private String USER_REFERENCE;
    private String META_REFERENCE;

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNoteListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userUID = auth.getCurrentUser().getUid();
        USER_REFERENCE = "users/" + userUID;
        META_REFERENCE = "users/" + userUID + "/notesMeta";

        NotesAdapter adapter = new NotesAdapter();
        adapter.setOnDeleteClickListener(note -> { // creating offering dialog to delete a note
                DeleteNoteDialog dialog = new DeleteNoteDialog(() -> deleteNote(note));
                dialog.show(getParentFragmentManager(), "dialog");
        });

        // Receiving user's data
        DocumentReference docRef = db.document(USER_REFERENCE);
        docRef.get() // gets user reference
                .continueWithTask(task -> createUserDocumentIfNotExists(task, docRef)) // creates user document if not exists
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get user's documents", e));

        // Listen changes in users/userUID/notesMeta and updating adapter
        CollectionReference notesRef = db.collection(META_REFERENCE);
        notesListener = notesRef.orderBy("lastUpdate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> fillAdapter(snapshots, e, adapter));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.noteFab.setOnClickListener(this::createNewNote);
    }

    @Nullable
    private Task<Void> createUserDocumentIfNotExists(@NonNull Task<DocumentSnapshot> task, DocumentReference docRef) {
        if (!task.getResult().exists()) {
            Log.d(TAG, "User's document doesn't exists. Creating.");
            return docRef.set(UserEntity.createDefaultUser());
        } else {
            Log.d(TAG, "User have documents.");
            return null;
        }
    }

    private void fillAdapter(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e, NotesAdapter adapter) {
        if (e != null) {
            Log.w(TAG, "Listen failed.", e);
            return;
        }

        if (snapshots != null) {
            Log.d(TAG, "notesListener is filling adapter");
            adapter.submitList(snapshots.getDocuments());
        } else {
            adapter.submitList(new ArrayList<>());
        }
    }

    /* TODO:
        - When document deleted its subcollections must be deleted too
     */
    private void deleteNote(DocumentSnapshot note) {
        String path = "users/" + userUID + "/notesMeta/" + note.getId();
        db.document(path).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Note has been deleted successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to delete note", e);
                    }
                });
    }

    private void createNewNote(View view) {
        NoteEntity newNote = new NoteEntity(NoteEntity.DEFAULT_NAME, NoteEntity.DEFAULT_CONTENT);
        Bundle bundle = new Bundle();
        bundle.putStringArray(NoteFragment.BUNDLE_KEY, new String[] {newNote.getName(), newNote.getContent()});
        Navigation.findNavController(view).navigate(R.id.action_noteListFragment_to_noteFragment, bundle);
    }
}