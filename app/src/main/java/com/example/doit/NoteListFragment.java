package com.example.doit;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doit.databinding.FragmentNoteListBinding;
import com.example.doit.entity.NoteEntity;
import com.example.doit.entity.UserEntity;
import com.example.doit.recyclerview.NotesAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";
    private final String CONTENT_MAIN_REFERENCE = "/content/main";

    private NavController navController;
    private FragmentNoteListBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration notesListener;

    private String userUID;
    private String USER_REFERENCE_PATH;
    private String META_REFERENCE_PATH;

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userUID = auth.getCurrentUser().getUid();
        USER_REFERENCE_PATH = "users/" + userUID;
        META_REFERENCE_PATH = "users/" + userUID + "/notesMeta";
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

        navController = Navigation.findNavController(view);

        NotesAdapter adapter = getNotesAdapter(); // just extracted long function

        // Receiving user's data
        DocumentReference docRef = db.document(USER_REFERENCE_PATH);
        docRef.get() // gets user reference
                .continueWithTask(task -> createUserDocumentIfNotExists(task, docRef)) // creates user document if not exists
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get user's documents", e));

        // Listen changes in users/userUID/notesMeta and updating adapter
        CollectionReference notesRef = db.collection(META_REFERENCE_PATH);
        notesListener = notesRef.orderBy("lastUpdate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> fillAdapter(snapshots, e, adapter));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.noteFab.setOnClickListener(view1 -> createDefaultNote());
    }

    @NonNull
    private NotesAdapter getNotesAdapter() {
        NotesAdapter adapter = new NotesAdapter();

        adapter.setOnDeleteClickListener(note -> { // creating offering dialog to delete a note
                DeleteNoteDialog dialog = new DeleteNoteDialog(() -> deleteNote(note));
                dialog.show(getParentFragmentManager(), "dialog");
        });

        adapter.setOnEditClickListener(noteSnapshot -> {
            openNote(noteSnapshot.getString("title"), noteSnapshot.getReference().getPath()); // title, path
        });

        return adapter;
    }

    private Task<Void> createUserDocumentIfNotExists(@NonNull Task<DocumentSnapshot> task, DocumentReference docRef) {
        if (!task.getResult().exists()) {
            Log.d(TAG, "User's document doesn't exists. Creating.");
            return docRef.set(UserEntity.createDefaultUser());
        } else {
            Log.d(TAG, "User have documents.");
            return Tasks.forResult(null);
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
        }
    }

    private void deleteNote(DocumentSnapshot note) {
        String metaPath = "users/" + userUID + "/notesMeta/" + note.getId();
        String contentPath = metaPath + "/content/main";
        db.document(contentPath).delete()
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        if (!task.isSuccessful()) throw task.getException();
                        return db.document(metaPath).delete();
                    }
                })
                .addOnSuccessListener(unused -> Log.d(TAG, "Note (" + note.getId() + ") and its content has been deleted successfully"))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Deleting note failed", e);
                    Toast.makeText(requireContext(), "Ошибка при удалении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createDefaultNote() { // creates default note
        DocumentReference noteDocRef = db.collection(META_REFERENCE_PATH).document();
        NoteEntity defaultNote = NoteEntity.createDefaultNote();
        noteDocRef.set(defaultNote)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Created note in db successfully");
                    openNote(defaultNote.getTitle(), noteDocRef.getPath());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Creating note in db is failed: ", e);
                    Toast.makeText(requireContext(), "Ошибка при создании записи: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openNote(String noteName, String notePath) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(NoteFragment.BUNDLE_KEY, new String[] {noteName, notePath});
        navController.navigate(R.id.action_noteListFragment_to_noteFragment, bundle);
    }
}