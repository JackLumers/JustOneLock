package janeelsmur.justonelock.objects;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import janeelsmur.justonelock.FolderActivity;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.utilities.DBTableHelper;

public class FolderFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener{

    /* СОХРАНЕННЫЕ ДАННЫЕ */
    private int folderIconResID;
    private String folderName;
    private String fullFilePath;
    private int folderId;
    private byte[] key;

    private LinearLayout folderLinearLayout;

    private DeleteDialog deleteDialog;

    //Аргументы
    public static FolderFragment newInstance(String fullFilePath, int folderIconResID, String folderName, int folderId) {
        Bundle args = new Bundle();
        FolderFragment fragment = new FolderFragment();

        args.putInt("folderId", folderId);
        args.putString("fullFilePath", fullFilePath);
        args.putInt("folderIconResID", folderIconResID);
        args.putString("folderName", folderName);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Аргументы
        folderId = getArguments().getInt("folderId");
        folderIconResID = getArguments().getShort("folderIconCode");
        folderName = getArguments().getString("folderName");
        fullFilePath = getArguments().getString("fullFilePath");

        deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_FOLDER, folderId, fullFilePath, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, null);

        //Иконка папки, выбранная пользователем
        //ImageView folderIconImageView = view.findViewById(R.id.folderIcon);
        //folderIconImageView.setImageResource(folderIconResID);

        //Название папки
        TextView folderNameTextView = view.findViewById(R.id.folderName);
        folderNameTextView.setText(folderName);

        //Кликабельный лэйаут кнопки
        folderLinearLayout = view.findViewById(R.id.folderLinearLayout);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        folderLinearLayout.setOnClickListener(this);
        folderLinearLayout.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.folderLinearLayout: //Открытие папки
                v.setOnClickListener(null);

                SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

                //Получение индекса папки для отправки системного имени в активность папки
                String whereClause = DBTableHelper.FOLDER_INDEXER_NAMES_FIELD + " = ?";
                String[] whereArgs = {folderName};
                Cursor cursor = database.query(DBTableHelper.FOLDER_INDEXER_TABLE, null, whereClause, whereArgs, null, null, null);
                cursor.moveToFirst();

                String systemFolderName = "folder" + cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID)); //Системное имя папки

                cursor.close();
                database.close();

                key = getActivity().getIntent().getByteArrayExtra("KEY");
                Intent intent = new Intent(getContext(), FolderActivity.class);
                intent.putExtra("folderId", folderId);
                intent.putExtra("KEY", key);
                intent.putExtra("folderName", folderName);
                intent.putExtra("fullFilePath", fullFilePath);
                intent.putExtra("systemFolderName", systemFolderName);
                startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        deleteDialog.show(getFragmentManager(), "deleteDialog");
        return false;
    }
}
