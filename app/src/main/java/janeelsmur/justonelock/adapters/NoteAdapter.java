package janeelsmur.justonelock.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import janeelsmur.justonelock.NoteActivity;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.objects.Note;

/**
 * Created by Ильназ on 20.02.2018.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {

    private ArrayList<Note> notes;
    private Context context;
    private String fullFilePath;
    private byte[] key;

    public NoteAdapter(ArrayList<Note> notes, String fullFilePath, byte[] key) {
        this.notes = notes;
        this.fullFilePath = fullFilePath;
        this.key = key;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_note, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.header.setText(notes.get(position).header);
        holder.text.setText(notes.get(position).text);
        holder.noteId = notes.get(position).noteId;
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private LinearLayout linearLayout;
        private TextView header, text;
        private int noteId;

        MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            linearLayout = view.findViewById(R.id.noteLinearLayout);
            header = (TextView) view.findViewById(R.id.nameTxt);
            text = (TextView) view.findViewById(R.id.noteTxt);

            linearLayout.setOnLongClickListener(this);
            linearLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, NoteActivity.class);
            intent.putExtra("KEY", key);
            intent.putExtra("fullFilePath", fullFilePath);
            intent.putExtra("noteId", noteId);
            v.getContext().startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            DeleteDialog deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_NOTE, fullFilePath, noteId, false);
            deleteDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "deleteDialog");
            return false;
        }
    }
}
