package com.example.doit.recyclerview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doit.DeleteNoteDialog;
import com.example.doit.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class NotesAdapter extends ListAdapter<DocumentSnapshot, NotesAdapter.ViewHolder> {

    private static final String TAG = "NotesAdapter";

    // For buttons on ViewHolder
    public interface onDeleteClickListener {
        void onDeleteClick(DocumentSnapshot note);
    }

    public interface onEditClickListener {
        void onEditClick(DocumentSnapshot note);
    }

    private onDeleteClickListener onDeleteClickListener;
    private onEditClickListener onEditClickListener;

    public NotesAdapter() {
        super(diffCallback);
    }

    public void setOnDeleteClickListener(NotesAdapter.onDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void setOnEditClickListener(NotesAdapter.onEditClickListener onEditClickListener) {
        this.onEditClickListener = onEditClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_card, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DocumentSnapshot documentSnapshot = getItem(position);
        HashMap<String, Object> note = (HashMap<String, Object>) documentSnapshot.getData();
        Log.i(TAG, "Binding new note with title: " + note.get("title"));
        holder.bind(note); // binding

        if (onDeleteClickListener != null) {
            holder.cardDelBtn.setOnClickListener(view -> onDeleteClickListener.onDeleteClick(documentSnapshot));
        }
        if (onEditClickListener != null) {
            holder.cardEditBtn.setOnClickListener(view -> onEditClickListener.onEditClick(documentSnapshot));
        }
    }

    public static final DiffUtil.ItemCallback<DocumentSnapshot> diffCallback =
            new DiffUtil.ItemCallback<DocumentSnapshot>() {
                @Override
                public boolean areItemsTheSame(@NonNull DocumentSnapshot oldItem, @NonNull DocumentSnapshot newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull DocumentSnapshot oldItem, @NonNull DocumentSnapshot newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cardName;
        TextView cardSnippet;
        TextView cardDate;
        Button cardEditBtn;
        Button cardDelBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setClickable(false);
            cardName = itemView.findViewById(R.id.noteCardName);
            cardSnippet = itemView.findViewById(R.id.noteCardSnippet);
            cardDate = itemView.findViewById(R.id.noteCardDate);
            cardEditBtn = itemView.findViewById(R.id.noteCardEditButton);
            cardDelBtn = itemView.findViewById(R.id.noteCardDeleteButton);
        }

        private void bind(HashMap<String, Object> note) {
            cardName.setText((String) note.get("title"));
            cardSnippet.setText((String) note.get("snippet"));

            // formating note's date
            String date = SimpleDateFormat.getDateInstance().format(
                    ((Timestamp) note.get("createdAt")).toDate());
            cardDate.setText(date);
        }
    }
}
