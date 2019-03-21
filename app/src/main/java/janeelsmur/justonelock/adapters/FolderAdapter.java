package janeelsmur.justonelock.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import janeelsmur.justonelock.FolderActivity;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.objects.Folder;
import janeelsmur.justonelock.utilities.DBTableHelper;

/**
 * Created by Ильназ on 20.02.2018.
 * Edited by Jack Lumers on 16.07.2018.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {

    private ArrayList<Folder> folder;
    private Context context;
    private String fullFilePath;
    private byte[] key;
    private DeleteDialog deleteDialog;

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

    //Выставение значений во вьющке при прогрузке
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.folderNameTextView.setText(folder.get(position).folderName);
        holder.folderId = folder.get(position).folderId;
        holder.folderName = folder.get(position).folderName;
    }

    @Override
    public int getItemCount() {
        return (folder == null) ? 0 : folder.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private LinearLayout linearLayout;
        private TextView folderNameTextView;
        private int folderId;
        private String folderName;

        MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            linearLayout = view.findViewById(R.id.folderLinearLayout);
            folderNameTextView = view.findViewById(R.id.folderName);
            linearLayout.setOnClickListener(this);
            linearLayout.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {

            SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

            //Получение индекса папки для отправки системного имени в активность папки
            String whereClause = DBTableHelper.FOLDER_INDEXER_NAMES_FIELD + " = ?";
            String[] whereArgs = {folderName};
            Cursor cursor = database.query(DBTableHelper.FOLDER_INDEXER_TABLE, null, whereClause, whereArgs, null, null, null);
            cursor.moveToFirst();
            //Системное имя папки. folder + индекс папки.
            String systemFolderName = "folder" + cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID)); //Системное имя папки

            cursor.close();
            database.close();

            key = ((AppCompatActivity) context).getIntent().getByteArrayExtra("KEY");
            Intent intent = new Intent(context, FolderActivity.class);
            intent.putExtra("folderId", folderId); //Id папки в таблице индексирования. Метка удаления производится по id.
            intent.putExtra("KEY", key); //Ключ
            intent.putExtra("folderName", folderName); //Название папки для тулбара
            intent.putExtra("fullFilePath", fullFilePath); //Полный путь к хранилищу
            intent.putExtra("systemFolderName", systemFolderName); //Системное название папки
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_FOLDER, folderId, fullFilePath, false);
            deleteDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "deleteDialog");
            return false;
        }
    }

}
