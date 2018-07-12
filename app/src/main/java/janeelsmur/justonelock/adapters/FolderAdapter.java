package janeelsmur.justonelock.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

import janeelsmur.justonelock.CreatePasswordActivity;
import janeelsmur.justonelock.FolderActivity;
import janeelsmur.justonelock.NoteActivity;
import janeelsmur.justonelock.PasswordActivity;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.objects.Folder;
import janeelsmur.justonelock.objects.Note;
import janeelsmur.justonelock.objects.Password;
import janeelsmur.justonelock.objects.PasswordFragment;
import janeelsmur.justonelock.utilites.DBTableHelper;

/**
 * Created by Ильназ on 20.02.2018.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {

    private ArrayList<Folder> folder;
    private Context context;
    private String fullFilePath;
    private byte[] key;
    private String foldername;
    private String systemFolderName = null;
    private DeleteDialog deleteDialog;
    private int folderId;

    public FolderAdapter(ArrayList<Folder> folder, String fullFilePath, byte[] key) {
        this.folder = folder;
        this.fullFilePath = fullFilePath;
        this.key = key;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_folder, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.folderName.setText(folder.get(position).foldername);
        holder.folderId=folder.get(position).folderId;
        holder.foldername=folder.get(position).foldername;



    }

    @Override
    public int getItemCount() {
        return (folder == null) ? 0 : folder.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private LinearLayout linearLayout;
        private TextView folderName;
        private int folderId;
        private String foldername;

        MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            linearLayout = view.findViewById(R.id.folderLinearLayout);
            folderName = (TextView) view.findViewById(R.id.folderName);
            linearLayout.setOnClickListener(this);
            linearLayout.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {

            SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

            //Получение индекса папки для отправки системного имени в активность папки
            String whereClause = DBTableHelper.FOLDER_INDEXER_NAMES_FIELD + " = ?";
            String[] whereArgs = {foldername};
            Cursor cursor = database.query(DBTableHelper.FOLDER_INDEXER_TABLE, null, whereClause, whereArgs, null, null, null);
            cursor.moveToFirst();

            String systemFolderName = "folder" + cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID)); //Системное имя папки

            cursor.close();
            database.close();

            key = ((AppCompatActivity) context).getIntent().getByteArrayExtra("KEY");
            Intent intent = new Intent(context, FolderActivity.class);
            intent.putExtra("folderId", folderId);
            intent.putExtra("KEY", key);
            intent.putExtra("folderName", foldername);
            intent.putExtra("fullFilePath", fullFilePath);
            intent.putExtra("systemFolderName", systemFolderName);
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_FOLDER, folderId,fullFilePath, false);
            deleteDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "deleteDialog");
            return false;
        }
    }

}
