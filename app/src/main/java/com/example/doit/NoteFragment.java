package com.example.doit;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doit.databinding.FragmentNoteBinding;
import com.example.doit.entity.ContentEntity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.spans.StrongEmphasisSpan;
import io.noties.markwon.editor.AbstractEditHandler;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;
import io.noties.markwon.editor.MarkwonEditorUtils;
import io.noties.markwon.editor.PersistedSpans;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.editor.MarkwonEditor;


public class NoteFragment extends Fragment {
    private static final String TAG = "NoteFragment";

    private NavController navController;
    private FragmentNoteBinding noteBinding;

    private Markwon markwon;
    private MarkwonEditor markwonEditor;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String NOTE_REFERENCE_PATH;
    private String CONTENT_REFERENCE_PATH;

    private String content;

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
        Context context = requireContext();
        markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(TaskListPlugin.create(context))
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();

        markwonEditor = MarkwonEditor.create(markwon);

        noteBinding = FragmentNoteBinding.inflate(inflater, container, false);
        return noteBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // loading content
        DocumentReference docRef = db.document(CONTENT_REFERENCE_PATH);
        docRef.get()
                .continueWithTask(task -> createContentDocumentIfNotExists(task, docRef)) // if content document doesn't exists creates and returns it
                                                                                // if exists returns it
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "Content loaded successfully");
                    content = result.getString("content");
                    markwon.setMarkdown(noteBinding.noteText, // sets up content text
                            content != null ? content : "");
                    noteBinding.noteName.setText(noteNameAndPath[0]);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Content loading failed: ", e);
                    Toast.makeText(requireContext(), "Ошибка при загрузке: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // fab logic
        view.post(() -> {
            float toolbarOffset = noteBinding.floatingToolbar.getHeight() + dpToPx(16);
            noteBinding.floatingToolbar.setTranslationY(toolbarOffset);
        });

        noteBinding.editFab.setOnClickListener(view1 -> {
            float fabOffset = view1.getHeight() + dpToPx(16); // 16 - margin
            view1.animate()
                    .translationY(fabOffset)
                    .setDuration(250)
                    .withEndAction(() -> view1.setVisibility(View.GONE))
                    .start();

            noteBinding.floatingToolbar.setVisibility(View.VISIBLE);
            noteBinding.floatingToolbar.animate()
                    .translationY(0)
                    .setDuration(250)
                    .start();

            noteBinding.noteText.setFocusable(true);
            noteBinding.noteText.setFocusableInTouchMode(true);
            noteBinding.noteText.setScreenReaderFocusable(true);
            noteBinding.noteText.requestFocus();
        });

        // toolbar logic
        noteBinding.floatingToolbarButtonDone.setOnClickListener(view2 -> {
            float toolbarOffset = noteBinding.floatingToolbar.getHeight() + dpToPx(16);
            noteBinding.floatingToolbar.animate()
                    .translationY(toolbarOffset)
                    .setDuration(250)
                    .withEndAction(() -> noteBinding.floatingToolbar.setVisibility(View.GONE))
                    .start();

            noteBinding.editFab.setVisibility(View.VISIBLE);
            noteBinding.editFab.animate()
                    .translationY(0)
                    .setDuration(250)
                    .start();

            noteBinding.noteText.setFocusable(false);
            noteBinding.noteText.setFocusableInTouchMode(false);
            noteBinding.noteText.setScreenReaderFocusable(false);

            String text = noteBinding.noteText.getText().toString();
            // saving in Firebase
            db.document(CONTENT_REFERENCE_PATH).set(new ContentEntity(text))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "Saved content successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Saving content failed: ", e);
                                Toast.makeText(requireContext(), "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            markwon.setMarkdown(noteBinding.noteText, text);
        });

        noteBinding.floatingToolbarButtonBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendEditTextWith("**");
            }
        });

        noteBinding.floatingToolbarButtonItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendEditTextWith("*");
            }
        });

        noteBinding.floatingToolbarButtonUnderlined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendEditTextWith("++");
            }
        });

        // markwon
        noteBinding.noteText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(markwonEditor));

        // back button
        noteBinding.noteBackButton.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.action_noteFragment_to_noteListFragment));
    }

    private void appendEditTextWith(String tag) {
        String currentText = noteBinding.noteText.getText().toString();

        int cursorPos = noteBinding.noteText.getSelectionStart();
        String textWithTag = new StringBuffer(currentText).insert(cursorPos, tag).toString();

        noteBinding.noteText.setText(textWithTag);
        noteBinding.noteText.setSelection(cursorPos + tag.length());
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

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}