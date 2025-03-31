package com.example.doit;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doit.databinding.FragmentNoteBinding;
import com.example.doit.databinding.FragmentNoteListBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteFragment extends Fragment {

    public static final String BUNDLE_KEY = "bundlekey";

    private String[] mParam1;

    public NoteFragment() {
        // Required empty public constructor
    }

    public static NoteFragment newInstance(String[] param) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putStringArray(BUNDLE_KEY, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getStringArray(BUNDLE_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentNoteBinding noteBinding = FragmentNoteBinding.inflate(inflater, container, false);
        View view = noteBinding.getRoot();
        noteBinding.noteName.setText(mParam1[0]);
        noteBinding.noteText.setText(mParam1[1]);
        noteBinding.noteBackButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_noteFragment_to_noteListFragment));
        return view;
    }
}