package com.example.doit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.doit.databinding.FragmentNoteBinding;
import com.example.doit.entity.ContentEntity;
import com.example.doit.entity.NoteEntity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.editor.MarkwonEditor;


public class NoteFragment extends Fragment {
    private static final String TAG = "NoteFragment";
    public static final String BUNDLE_KEY = "note_bundle_key";

    private static final int MARGIN = 16;

    private NavController navController;
    private FragmentNoteBinding noteBinding;

    private Markwon markwon;
    private MarkwonEditor markwonEditor;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String NOTE_REFERENCE_PATH;
    private String CONTENT_REFERENCE_PATH;
    private String NOTE_NAME;

    private String content;
    private String contentTextWithTags;
    private boolean editing = false;

    private String[] noteNameAndPath;

    public NoteFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteNameAndPath = getArguments().getStringArray(BUNDLE_KEY);

            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            NOTE_NAME = noteNameAndPath[0];
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

        var floatingToolbar = noteBinding.floatingToolbar;
        float toolbarOffset = floatingToolbar.getHeight() + dpToPx(MARGIN, requireActivity());

        // content loading from db logic
        DocumentReference docRef = db.document(CONTENT_REFERENCE_PATH);
        docRef.get()
                .continueWithTask(task -> createContentDocumentIfNotExists(task, docRef)) // if content document doesn't exists creates and returns it
                                                                                // if exists returns it
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "Content loaded successfully");
                    contentTextWithTags = result.getString("content");
                    fillNoteFromSnapshot(result);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Content loading failed: ", e);
                    Toast.makeText(requireContext(), "Ошибка при загрузке: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // translating toolbar
        view.post(() -> {
            float offset = floatingToolbar.getHeight() + dpToPx(MARGIN, requireActivity());
            floatingToolbar.setTranslationY(offset);
        });

        // click on content text
        noteBinding.noteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editing) {
                    makeVisible(floatingToolbar);
                    makeFocusable(noteBinding.noteText, true);
                    noteBinding.noteText.setText(contentTextWithTags);
                    noteBinding.noteText.requestFocus();
                    editing = true;
                }
            }
        });

        // toolbar logic
        noteBinding.floatingToolbarButtonDone.setOnClickListener(view2 -> {
            editing = false;

            // DONE BUTTON
            contentTextWithTags = noteBinding.noteText.getText().toString();
            markwon.setMarkdown(noteBinding.noteText, contentTextWithTags);
//            db.document(CONTENT_REFERENCE_PATH).set(new ContentEntity(contentTextWithTags))
//                    .continueWithTask((Continuation<Void, Task<Void>>) task -> {
//                        if (!task.isSuccessful()) throw task.getException();
//
//                        markwon.setMarkdown(noteBinding.noteText, contentTextWithTags);
//                        return Tasks.forResult(null);
//                    })
//                    .addOnSuccessListener(unused -> Log.d(TAG, "Saved content successfully"))
//                    .addOnFailureListener(e -> {
//                        Log.w(TAG, "Saving content failed: ", e);
//                        Toast.makeText(requireContext(), "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });

            makeInvisible(floatingToolbar, toolbarOffset);
            makeFocusable(noteBinding.noteText, false);
        });

        // BOLD BUTTON
        noteBinding.floatingToolbarButtonBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendEditTextWith("<b>");
            }
        });

        // ITALIC BUTTON
        noteBinding.floatingToolbarButtonItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendEditTextWith("<i>");
            }
        });

        // UNDERLINED BUTTON
        noteBinding.floatingToolbarButtonUnderlined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendEditTextWith("<u>");
            }
        });

        // markwon editor
        noteBinding.noteText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(markwonEditor));

        // back button
        noteBinding.noteBackButton.setOnClickListener(v -> Navigation.findNavController(view)
                .navigate(R.id.action_noteFragment_to_noteListFragment));
    }

    private static void makeVisible(View view) {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .setDuration(250)
                .start();
    }

    private static void makeInvisible(View view, float offset) {
        view.animate()
                .translationY(offset)
                .setDuration(250)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    private void makeFocusable(View view, boolean focusable) {
        view.setFocusable(focusable);
        view.setFocusableInTouchMode(focusable);
        view.setScreenReaderFocusable(focusable);
    }

    private void fillNoteFromSnapshot(DocumentSnapshot result) {
        content = result.getString("content");
        markwon.setMarkdown(noteBinding.noteText, // sets up content
                content != null ? content : "");
        noteBinding.noteName.setText(NOTE_NAME);
    }

    private void appendEditTextWith(String tag) {
        Editable currentText = noteBinding.noteText.getText();

        int cursorStartPos = noteBinding.noteText.getSelectionStart();
        int cursorEndPos = noteBinding.noteText.getSelectionEnd();

        if (cursorStartPos < 0 || cursorEndPos < 0) return;

        String closeTag = "</" + tag.substring(1);

        if (cursorStartPos == cursorEndPos) {
            String beforeCursor = currentText.subSequence(0, cursorStartPos).toString();
            int openCount = countOccurrences(beforeCursor, tag);
            int closeCount = countOccurrences(beforeCursor, closeTag);
            String tagToInsert = (openCount > closeCount) ? closeTag : tag;

            currentText.insert(cursorStartPos, tagToInsert);
            noteBinding.noteText.setSelection(cursorStartPos + tagToInsert.length());
        } else {
            currentText.insert(cursorEndPos, closeTag);
            currentText.insert(cursorStartPos, tag);
            noteBinding.noteText.setSelection(cursorStartPos + tag.length(), cursorEndPos + tag.length());
        }
    }

    private int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
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

    @Override
    public void onPause() {
        if (editing) contentTextWithTags = noteBinding.noteText.getText().toString();

        // updating db
        db.document(CONTENT_REFERENCE_PATH).set(new ContentEntity(contentTextWithTags))
                .addOnSuccessListener(unused -> Log.d(TAG, "Saved content successfully"))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Saving content failed: ", e);
                    Toast.makeText(requireContext(), "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        String noteName = noteBinding.noteName.getText().toString();
        String textWithMarkdown = markwon.toMarkdown(contentTextWithTags).toString();
        String snippet = textWithMarkdown.substring(0, Math.min(textWithMarkdown.length(), NoteEntity.SNIPPET_LENGHT));
        db.document(NOTE_REFERENCE_PATH).update(
                NoteEntity.TITLE_FIELD, noteName,
                NoteEntity.SNIPPET_FIELD, snippet,
                NoteEntity.LAST_UPDATE_FIELD, Timestamp.now())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Updated note info successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Saving content failed: ", e);
                        Toast.makeText(requireContext(), "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        super.onPause();
    }

    private static float dpToPx(float dp, Activity activity) {
        return dp * activity.getResources().getDisplayMetrics().density;
    }
}