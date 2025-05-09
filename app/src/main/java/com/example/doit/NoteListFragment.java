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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NoteListFragment extends Fragment {

    private static final String TAG = "NoteListFragment";

    private FragmentNoteListBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userUID;

    public NoteListFragment() {
        // Required empty public constructor
    }

    public static NoteListFragment newInstance(String param1, String param2) {
        NoteListFragment fragment = new NoteListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        createUserDocumentIfNotExist();

        ArrayList<DocumentSnapshot> notes = new ArrayList<>();
        NotesAdapter adapter = new NotesAdapter(notes);
        fillNotesList(notes, adapter);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.noteFab.setOnClickListener(this::createNewNote);
    }

    private void fillNotesList(ArrayList<DocumentSnapshot> notes, NotesAdapter adapter) {
        String collectionPath = "users/" + userUID + "/notesMeta";
        db.collection(collectionPath).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "User has no documents. Collection " + collectionPath + " is empty");
                    } else {
                        Log.d(TAG, "User have documents. Loading collection " + collectionPath);
                        notes.addAll(queryDocumentSnapshots.getDocuments());
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load user's documents", e));
    }

    private void createUserDocumentIfNotExist() {
        DocumentReference docRef = db.document("users/" + userUID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) { // if not exists
                        Log.d(TAG, "User's document doesn't exists. Creating.");
                        docRef.set(UserEntity.createDefaultUser()); // creates default user settings
                    }
                    else Log.d(TAG, "User's document already exists");
                });
    }

    private void createNewNote(View view) {
        NoteEntity newNote = new NoteEntity(NoteEntity.DEFAULT_NAME, NoteEntity.DEFAULT_CONTENT);
        Bundle bundle = new Bundle();
        bundle.putStringArray(NoteFragment.BUNDLE_KEY, new String[] {newNote.getName(), newNote.getContent()});
        Navigation.findNavController(view).navigate(R.id.action_noteListFragment_to_noteFragment, bundle);
    }
}