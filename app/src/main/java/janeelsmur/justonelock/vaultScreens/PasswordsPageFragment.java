package janeelsmur.justonelock.vaultScreens;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.objects.PasswordFragment;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.objects.FolderFragment;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.utilites.NotificationListener;

import java.util.ArrayList;

public class PasswordsPageFragment extends Fragment {

    private String TAG = "PasswordsPageLogs";

    //Сохраненные в интенте значения
    private String fullFilePath;
    private byte[] key;

    private SQLiteDatabase database;

    //Фрагменты папок
    private ArrayList<FolderFragment> folderFragments = new ArrayList<>();
    //Фрагменты пароей
    private ArrayList<PasswordFragment> passwordFragments = new ArrayList<>();

    //Заглушка
    private RelativeLayout tempRelativeLayout;
    private RelativeLayout groupedText;
    private RelativeLayout foldersText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_passwords, null);
        folderFragments.toArray();

        tempRelativeLayout = view.findViewById(R.id.tempPasswordsRelativeLayout);
        groupedText = view.findViewById(R.id.grouped_text_relative_layout);
        foldersText = view.findViewById(R.id.folders_text_relative_layout);

        fullFilePath = getActivity().getIntent().getExtras().getString("fullFilePath");
        key = getActivity().getIntent().getByteArrayExtra("KEY");

        Log.d(TAG, "onStart: BackStackFragmentCount: " + getActivity().getSupportFragmentManager().getBackStackEntryCount());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            cleanFragments();
            loadFoldersFromDatabase();
            loadPasswordsFromDatabase();
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

    private void loadFoldersFromDatabase() {
        int fragmentsCount = 0;

        String whereClause = "isRemoved = ?";
        String[] whereArgs = new String[] {"0"};

        database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.FOLDER_INDEXER_TABLE, null, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {
            String folderName = cursor.getString(cursor.getColumnIndex(DBTableHelper.FOLDER_INDEXER_NAMES_FIELD));
            int folderId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
            folderFragments.add(FolderFragment.newInstance(fullFilePath, 0, folderName, folderId));
            getChildFragmentManager().beginTransaction().add(R.id.folders_container, folderFragments.get(fragmentsCount), "Folder").commit();
            fragmentsCount++;
            while (cursor.moveToNext()) {
                folderName = cursor.getString(cursor.getColumnIndex(DBTableHelper.FOLDER_INDEXER_NAMES_FIELD));
                folderId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
                folderFragments.add(FolderFragment.newInstance(fullFilePath, 0, folderName, folderId));
                getChildFragmentManager().beginTransaction().add(R.id.folders_container, folderFragments.get(fragmentsCount), "Folder").commit();
                fragmentsCount++;
            }
        }

        cursor.close();
        database.close();
    }

    private void loadPasswordsFromDatabase() {
        int fragmentsCount = 0;

        String whereClause = "isRemoved = ?";
        String[] whereArgs = new String[] {"0"};

        database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER, null, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {

            String title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
            String description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
            int passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

            passwordFragments.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));
            getChildFragmentManager().beginTransaction().add(R.id.passwords_without_folder_container, passwordFragments.get(fragmentsCount), "Password").commit();
            fragmentsCount++;

            while (cursor.moveToNext()) {
                title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
                description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));
                passwordFragments.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, DBTableHelper.TABLE_PASSWORDS_WITHOUT_FOLDER));
                getChildFragmentManager().beginTransaction().add(R.id.passwords_without_folder_container, passwordFragments.get(fragmentsCount), "Password").commit();
                fragmentsCount++;
            }
        }

        cursor.close();
        database.close();
    }

    private void cleanFragments(){
        try {
            //Очистка прошлых фрагментов
            for (FolderFragment folderFragment : folderFragments) {
                getChildFragmentManager().beginTransaction().remove(folderFragment).commit();
            }
            //Обнуляем
            folderFragments.clear();
            //Очистка прошлых фрагментов
            for (PasswordFragment passwordFragment : passwordFragments) {
                getChildFragmentManager().beginTransaction().remove(passwordFragment).commit();
            }
            //Обнуляем
            passwordFragments.clear();
        } catch (Exception e){
            Log.i("Очистка фрагментов", "Очистить фрагменты пока невозможно, так как вьюшка сохранила своё состояние и заморозилась.");
        }
    }

    /** Уведомляет фрагмент о том, что данные в нём изменились */
    public void notifyDataHasChanged() {
        try {
            cleanFragments();
            loadFoldersFromDatabase();
            loadPasswordsFromDatabase();

            //Ставим или убираем заглушку
            if (folderFragments.size() == 0 && passwordFragments.size() == 0)
                tempRelativeLayout.setVisibility(View.VISIBLE);
            else tempRelativeLayout.setVisibility(View.GONE);
            if (folderFragments.size() != 0) foldersText.setVisibility(View.VISIBLE);
            else foldersText.setVisibility(View.GONE);
            if (passwordFragments.size() != 0) groupedText.setVisibility(View.VISIBLE);
            else groupedText.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.i("Обновление фрагментов", "Обновить фрагменты пока невозможно, так как вьюшка сохранила своё состояние и заморозилась.");
        }
    }
}
