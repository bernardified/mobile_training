package com.shopback.notepad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NotesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Note> items;
    private final int NOTE = 0;

    NotesRecyclerAdapter(List<Note> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch(viewType) {
            case NOTE:
                View noteView = inflater.inflate(R.layout.note_row, parent, false);
                return new NoteViewHolder(noteView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }

        switch (holder.getItemViewType()) {
            case NOTE:
                NoteViewHolder nvh = (NoteViewHolder) holder;
                nvh.configure(position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType().ordinal();
    }

    void addNewNote(List<Note> newList) {
        items.clear();
        items.addAll(newList);
        notifyItemChanged(items.size()-1);
    }

    void updateNote(List<Note> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }

    void deleteNote(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView dateView;
        long rowId;

        NoteViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.recycler_note_title);
            dateView = itemView.findViewById(R.id.recycler_note_last_updated);
        }

        void configure(int position) {
            Note note = items.get(position);
            rowId = note.getId();
            titleView.setText(note.getTitle());
            dateView.setText(note.getLastUpdated());
        }

        long getId() {
            return rowId;
        }
    }
}
