package com.example.doit.recyclerview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doit.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private ArrayList<DocumentSnapshot> notes;

    public NotesAdapter(ArrayList<DocumentSnapshot> notes) {
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
        Log.i("TESTING", (String) note.get("title"));
        holder.cardName.setText((String) note.get("title"));
        holder.cardSnippet.setText((String) note.get("snippet"));
        holder.cardDate.setText(((Timestamp) note.get("createdAt")).toDate().toString());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

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
    }
}
