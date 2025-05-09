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

import com.example.doit.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class NotesAdapter extends ListAdapter<DocumentSnapshot, NotesAdapter.ViewHolder> {
    private static final String TAG = "NotesAdapter";

    private ArrayList<DocumentSnapshot> notes;

    public NotesAdapter(ArrayList<DocumentSnapshot> notes) {
        super(diffCallback);
        this.notes = notes;
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
        HashMap<String, Object> note = (HashMap<String, Object>) notes.get(position).getData();
        Log.i(TAG, "Binding new note with title:" + note.get("title"));
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
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
