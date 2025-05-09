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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";

    private FragmentNoteListBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userUID;

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

        ArrayList<DocumentSnapshot> notes = new ArrayList<>();
        NotesAdapter adapter = new NotesAdapter(notes);
        adapter.setOnDeleteClickListener(note -> {
                DeleteNoteDialog dialog = new DeleteNoteDialog(() -> deleteNote(note));
                dialog.show(getParentFragmentManager(), "dialog");
        });

        // Receiving user's data and filling up ArrayList with notes
        DocumentReference docRef = db.document("users/" + userUID);
        docRef.get()
                .continueWithTask(documentSnapshotTask -> getUserNotesDocuments(documentSnapshotTask, docRef))
                .addOnSuccessListener(queryDocumentSnapshots -> fillNotesWithUserData(queryDocumentSnapshots, notes, adapter))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load user's documents", e));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.noteFab.setOnClickListener(this::createNewNote);
    }

    private void fillNotesWithUserData(QuerySnapshot queryDocumentSnapshots, ArrayList<DocumentSnapshot> notes, NotesAdapter adapter) {
        String path = "users/" + userUID + "/notesMeta";
        if (queryDocumentSnapshots.isEmpty()) {
            Log.d(TAG, "User has no documents. Collection " + path + " is empty");
        } else {
            Log.d(TAG, "User have documents. Loading collection " + path);
            notes.addAll(queryDocumentSnapshots.getDocuments());
            adapter.submitList(notes); // updating recyclerview's adapter
        }
    }

    @NonNull
    private Task<QuerySnapshot> getUserNotesDocuments(Task<DocumentSnapshot> task, DocumentReference docRef) {
        if (!task.getResult().exists()) { // if user's data document not exists
            Log.d(TAG, "User's document doesn't exists. Creating.");
            docRef.set(UserEntity.createDefaultUser()); // creates default user data
        }
        else Log.d(TAG, "User's document already exists");

        return docRef.collection("notesMeta").get();
    }

    /* TODO:
        - When document deleted its subcollections must be deleted too
        - onSuccess adapter must be updated
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