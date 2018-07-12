package janeelsmur.justonelock.vaultScreens;

import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import janeelsmur.justonelock.R;
import janeelsmur.justonelock.adapters.FolderAdapter;
import janeelsmur.justonelock.adapters.PasswordAdapter;
import janeelsmur.justonelock.objects.Password;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.objects.PasswordFragment;

import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.Inflater;

public class FavoritesPageFragment extends Fragment {

    /* СОХРАНЕННЫЕ ДАННЫЕ */
    private String fullFilePath;
    private byte[] key;

    private SQLiteDatabase database;
    private PasswordAdapter Adapter, mAdapter;
    //Для обновления фрагментов
    private ArrayList<Password> passwordsInFolder = new ArrayList<>();
    private ArrayList<Password> passwordsWithoutFolder = new ArrayList<>();
    private RecyclerView recyclerViewpass,recyclerviewpassinFolder;
    //Заглушка
    private RelativeLayout tempRelativeLayout;
    private Inflater inflater;
    private Parcelable listState;
    private final String KEY_RECYCLER_STATE="recycler_state";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable  Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_page_favorites, null);
        setRetainInstance(false);
        passwordsInFolder.toArray();

        tempRelativeLayout = view.findViewById(R.id.tempFavoritesRelativeLayout);
        recyclerViewpass= view.findViewById(R.id.recyclerview_favourites);
        recyclerviewpassinFolder =view.findViewById(R.id.recyclerview_favourites_inFolder);
        /* СОХРАНЕННЫЕ ДАННЫЕ */
        fullFilePath = getActivity().getIntent().getExtras().getString("fullFilePath");
        key = getActivity().getIntent().getExtras().getByteArray("KEY");
        recyclerViewpass.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerviewpassinFolder.setLayoutManager(new LinearLayoutManager(getContext()));
        Adapter = new PasswordAdapter(passwordsWithoutFolder, fullFilePath, key);
        mAdapter = new PasswordAdapter(passwordsInFolder, fullFilePath, key);

        Log.w("FavoritesPage", "onStart: onCreateView сработал " );
        recyclerViewpass.setAdapter(Adapter);
        recyclerviewpassinFolder.setAdapter(mAdapter);


        return view;
    }



    @Override
    public void onStart() {



        //Загрузка избранных паролей


        try {
            passwordsInFolder.clear();
            passwordsWithoutFolder.clear();
            loadFavoritesPasswords();
            loadFavoritePasswordsFromFolders();
            Adapter.notifyDataSetChanged();
            mAdapter.notifyDataSetChanged();
            notifyDataHasChanged();
        } catch (Exception e) {
            Log.w("FavoritesPage", "onStart: " + e.getLocalizedMessage());
        }

        //Ставим или убираем заглушку
        if (passwordsInFolder.size()==0 && passwordsWithoutFolder.size()==0) tempRelativeLayout.setVisibility(View.VISIBLE);
            else tempRelativeLayout.setVisibility(View.GONE);

        super.onStart();
    }


    private void loadFavoritesPasswords(){
        int fragmentsCount = 0;

        String whereClause = "isFavorite = ? AND isRemoved = ?";
        String[] whereArgs = new String[] {"1", "0"};

        database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER, null, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {

            String title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
            String description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
            int passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

            passwordsWithoutFolder.add(new Password(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));


            while (cursor.moveToNext()) {
                title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
                description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
                passwordsWithoutFolder.add(new Password(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));

            }
        }

        cursor.close();
        database.close();
    }

    private void loadFavoritePasswordsFromFolders(){
        int fragmentsCount = 0;

        database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        //Получаем массив с id всех папок
        String whereClause = "isRemoved = ?";
        String[] whereArgs = new String[] {"0"};
        Cursor cursor = database.query(DBTableHelper.FOLDER_INDEXER_TABLE, null, whereClause, whereArgs, null, null, null);
        Vector<Integer> folderIndexes = new Vector<>();
        folderIndexes.toArray();
        if(cursor.moveToFirst()){
            folderIndexes.add(cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID)));
            while (cursor.moveToNext()){
                folderIndexes.add(cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID)));
            }
        }

        whereClause = "isFavorite = ? AND isRemoved = ?";
        whereArgs = new String[] {"1", "0"};
        String systemFolderNamePrefix = DBTableHelper.SYSTEM_FOLDER_NAME_PREFIX;

        for(int i = 0; i<folderIndexes.size(); i++) { //Перебираем папки, пока не дойдем до конца массива
            int folderIndex = folderIndexes.get(i); //Индекс папки
            String systemFolderName = systemFolderNamePrefix + folderIndex;
            cursor = database.query(systemFolderName, null, whereClause, whereArgs, null, null, null);

            if (cursor.moveToFirst()) {
                String title = (FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key));
                String description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                int passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

                passwordsInFolder.add(new Password(title, description, 0, passwordId, fullFilePath, systemFolderName));

                while (cursor.moveToNext()) { //Пока есть строки с паролями, перебираем эти строки

                    title = (FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key));
                    description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                    passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

                    passwordsInFolder.add(new Password(title, description, 0, passwordId, fullFilePath, systemFolderName));

                }
            }
        }

        cursor.close();
        database.close();
    }

    /** Уведомляет фрагмент о том, что данные в нём изменились */
    public void notifyDataHasChanged() {
        try {
            //Загрузка избранных паролей
            passwordsInFolder.clear();
            passwordsWithoutFolder.clear();
            loadFavoritesPasswords();
            loadFavoritePasswordsFromFolders();
            Adapter.notifyDataSetChanged();
            mAdapter.notifyDataSetChanged();

            //Ставим или убираем заглушку
            if (passwordsInFolder.size() == 0 && passwordsWithoutFolder.size() == 0)
                tempRelativeLayout.setVisibility(View.VISIBLE);
            else tempRelativeLayout.setVisibility(View.GONE);
        } catch (Exception e){
            Log.i("FavouritesPage", "Обновить фрагменты пока невозможно, так как вьюшка сохранила своё состояние и заморозилась.");
        }
    }
}
