package janeelsmur.justonelock.vaultScreens;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.adapters.FolderAdapter;
import janeelsmur.justonelock.adapters.PasswordAdapter;
import janeelsmur.justonelock.objects.Folder;
import janeelsmur.justonelock.objects.Password;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;

import java.util.ArrayList;

public class PasswordsPageFragment extends Fragment {

    private String TAG = "PasswordsPageLogs";

    //Сохраненные в интенте значения
    private String fullFilePath;
    private byte[] key;

    private SQLiteDatabase database;

    private RecyclerView recyclerViewpassword, recyclerViewfolder;
    private PasswordAdapter mAdapter;
    private FolderAdapter Adapter;

    //Фрагменты папок
    private ArrayList<Folder> folderFragments = new ArrayList<>();
    //Фрагменты пароей
    private ArrayList<Password> passwordFragments = new ArrayList<>();

    //Заглушка
    private RelativeLayout tempRelativeLayout;
    private RelativeLayout groupedText;
    private RelativeLayout foldersText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_passwords, null);

        // СОХРАНЕННЫЕ ДАННЫЕ //
        fullFilePath = getActivity().getIntent().getStringExtra("fullFilePath");
        key = getActivity().getIntent().getByteArrayExtra("KEY");

        folderFragments.toArray();
        tempRelativeLayout = view.findViewById(R.id.tempPasswordsRelativeLayout);
        groupedText = view.findViewById(R.id.grouped_text_relative_layout);
        foldersText = view.findViewById(R.id.folders_text_relative_layout);
        recyclerViewfolder = view.findViewById(R.id.recyclerview_folder);
        recyclerViewpassword = view.findViewById(R.id.recyclerview_password);

        Log.d(TAG, "onStart: BackStackFragmentCount: " + getActivity().getSupportFragmentManager().getBackStackEntryCount());
        recyclerViewpassword.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new PasswordAdapter(passwordFragments, fullFilePath, key);
        recyclerViewpassword.setAdapter(mAdapter);
        recyclerViewfolder.setLayoutManager(new LinearLayoutManager(getContext()));
        Adapter = new FolderAdapter(folderFragments, fullFilePath, key);
        recyclerViewfolder.setAdapter(Adapter);

        Log.d("PasswordsPage", "onCreateView!" + fullFilePath);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        try {
            folderFragments.clear();
            passwordFragments.clear();
            loadFoldersFromDatabase();
            loadPasswordsFromDatabase();
            mAdapter.notifyDataSetChanged();
            Adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "onStart: " + e.getLocalizedMessage());
        }

        //Ставим или убираем заглушку
        if (folderFragments.size()==0 && passwordFragments.size()==0) tempRelativeLayout.setVisibility(View.VISIBLE);
            else tempRelativeLayout.setVisibility(View.GONE);
        if (folderFragments.size()!=0) foldersText.setVisibility(View.VISIBLE);
            else foldersText.setVisibility(View.GONE);
        if (passwordFragments.size()!=0) groupedText.setVisibility(View.VISIBLE);
            else groupedText.setVisibility(View.GONE);
    }

    /** Уведомляет фрагмент о том, что данные в нём изменились */
    public void notifyDataHasChanged() {
        try {
            folderFragments.clear();
            passwordFragments.clear();
            loadFoldersFromDatabase();
            loadPasswordsFromDatabase();
            mAdapter.notifyDataSetChanged();
            Adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.w("passwordPage", "notifyDataHasChanged: " + e.getLocalizedMessage());
        }

        //Ставим или убираем заглушку
        if (folderFragments.size() == 0 && passwordFragments.size() == 0)
            tempRelativeLayout.setVisibility(View.VISIBLE);
        else tempRelativeLayout.setVisibility(View.GONE);
        if (folderFragments.size() != 0) foldersText.setVisibility(View.VISIBLE);
        else foldersText.setVisibility(View.GONE);
        if (passwordFragments.size() != 0) groupedText.setVisibility(View.VISIBLE);
        else groupedText.setVisibility(View.GONE);


    }


    private void loadFoldersFromDatabase() {


        String whereClause = "isRemoved = ?";
        String[] whereArgs = new String[] {"0"};
        Log.i("PasswordPage", fullFilePath);
        database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.FOLDER_INDEXER_TABLE, null, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {
            String folderName = cursor.getString(cursor.getColumnIndex(DBTableHelper.FOLDER_INDEXER_NAMES_FIELD));
            int folderId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
            folderFragments.add(new Folder(fullFilePath, 0, folderName, folderId));
            while (cursor.moveToNext()) {
                folderName = cursor.getString(cursor.getColumnIndex(DBTableHelper.FOLDER_INDEXER_NAMES_FIELD));
                folderId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
                folderFragments.add(new Folder(fullFilePath, 0, folderName, folderId));
            }
        }

        cursor.close();
        database.close();
    }

    private void loadPasswordsFromDatabase() {

        String whereClause = "isRemoved = ?";
        String[] whereArgs = new String[] {"0"};

        database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER, null, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {

            String title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
            String description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
            int passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

            passwordFragments.add(new Password(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));

            while (cursor.moveToNext()) {
                title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
                description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
                passwordFragments.add(new Password(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));
            }
        }

        cursor.close();
        database.close();
    }


}
