package janeelsmur.justonelock.vaultScreens;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.objects.PasswordFragment;

import java.util.ArrayList;
import java.util.Vector;

public class FavoritesPageFragment extends Fragment {

    /* СОХРАНЕННЫЕ ДАННЫЕ */
    private String fullFilePath;
    private byte[] key;

    private SQLiteDatabase database;

    //Для обновления фрагментов
    private ArrayList<Fragment> passwordFragments = new ArrayList<>();
    private ArrayList<Fragment> passwordsWithoutFolder = new ArrayList<>();

    //Заглушка
    private RelativeLayout tempRelativeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_page_favorites, null);
        passwordFragments.toArray();

        tempRelativeLayout = view.findViewById(R.id.tempFavoritesRelativeLayout);

        /* СОХРАНЕННЫЕ ДАННЫЕ */
        fullFilePath = getActivity().getIntent().getExtras().getString("fullFilePath");
        key = getActivity().getIntent().getExtras().getByteArray("KEY");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Загрузка избранных паролей
        try {
            cleanFragments();
            loadFavoritesPasswords();
            loadFavoritePasswordsFromFolders();
        } catch (Exception e) {
            Log.w("FavoritesPage", "onStart: " + e.getLocalizedMessage());
        }

        //Ставим или убираем заглушку
        if (passwordFragments.size()==0 && passwordsWithoutFolder.size()==0) tempRelativeLayout.setVisibility(View.VISIBLE);
            else tempRelativeLayout.setVisibility(View.GONE);
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

            passwordsWithoutFolder.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));
            getChildFragmentManager().beginTransaction().add(R.id.favorites_container, passwordsWithoutFolder.get(fragmentsCount)).commit();
            fragmentsCount++;

            while (cursor.moveToNext()) {
                title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
                description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
                passwordsWithoutFolder.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));
                getChildFragmentManager().beginTransaction().add(R.id.favorites_container, passwordsWithoutFolder.get(fragmentsCount)).commit();
                fragmentsCount++;
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

                passwordFragments.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, systemFolderName));
                getChildFragmentManager().beginTransaction().add(R.id.favorites_container, passwordFragments.get(fragmentsCount)).commit();
                fragmentsCount++;

                while (cursor.moveToNext()) { //Пока есть строки с паролями, перебираем эти строки

                    title = (FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key));
                    description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                    passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

                    passwordFragments.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, systemFolderName));
                    getChildFragmentManager().beginTransaction().add(R.id.favorites_container, passwordFragments.get(fragmentsCount)).commit();
                    fragmentsCount++;
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
            cleanFragments();
            loadFavoritesPasswords();
            loadFavoritePasswordsFromFolders();

            //Ставим или убираем заглушку
            if (passwordFragments.size() == 0 && passwordsWithoutFolder.size() == 0)
                tempRelativeLayout.setVisibility(View.VISIBLE);
            else tempRelativeLayout.setVisibility(View.GONE);
        } catch (Exception e){
            Log.i("Оновлекние фрагментов", "Обновить фрагменты пока невозможно, так как вьюшка сохранила своё состояние и заморозилась.");
        }
    }

    private void cleanFragments() {
        try {
            //Очистка прошлых фрагментов
            for (Fragment passwordFragment : passwordFragments) {
                getChildFragmentManager().beginTransaction().remove(passwordFragment).commit();
            }
            for (Fragment passwordFragment : passwordsWithoutFolder) {
                getChildFragmentManager().beginTransaction().remove(passwordFragment).commit();
            }
            //Обнуляем
            passwordFragments.clear();
            passwordsWithoutFolder.clear();
        } catch (Exception e) {
            Log.i("Очистка фрагментов", "Очистить фрагменты пока невозможно, так как вьюшка сохранила своё состояние и заморозилась.");
        }
    }

}
