package janeelsmur.justonelock;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.objects.PasswordFragment;
import janeelsmur.justonelock.utilites.NotificationListener;

import java.util.ArrayList;

public class FolderActivity extends AppCompatActivity implements View.OnClickListener, NotificationListener {

    private final String TAG = "FolderActivity";
    private final int ADD_PASS_AFTER_CREATING = 1;

    /* СОХРАНЕННЫЕ ДАННЫЕ */
    private String fullFilePath;
    private String systemFolderName;
    private String folderName;
    private int folderId;
    private byte[] key;

    private SQLiteDatabase database;
    private ArrayList<PasswordFragment> passwordFragments = new ArrayList<>();

    private Menu globalMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        passwordFragments.toArray();

        /* СОХРАНЕННЫЕ ДАННЫЕ */
        fullFilePath = getIntent().getExtras().getString("fullFilePath");
        systemFolderName = getIntent().getExtras().getString("systemFolderName");
        folderName = getIntent().getExtras().getString("folderName");
        key = getIntent().getExtras().getByteArray("KEY");
        folderId = getIntent().getExtras().getInt("folderId");

        Toolbar toolbar = (Toolbar) findViewById(R.id.folderToolbar);
        toolbar.setTitle(folderName);
        setSupportActionBar(toolbar);
        //Показ кнопки "назад"
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButtonCreatePassword);
        fab.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        reloadPasswordsFromDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        globalMenu = menu;
        menu.getItem(2).setVisible(true);
        menu.getItem(3).setVisible(true);
        return true;
    }

    @Override
    public void onNotificationTaken(int notification) {
        switch (notification){
            case NotificationListener.FINISH:
                finish();
                break;

            case NotificationListener.DATA_CHANGED:
                reloadPasswordsFromDatabase();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_remove:
                DeleteDialog deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_FOLDER, folderId, fullFilePath, true);
                deleteDialog.show(getSupportFragmentManager(), "deleteDialog");
                break;

            case R.id.action_edit: break;

            default: finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadPasswordsFromDatabase() {
            //Удаление всех фрагментов
            for(int i = 0; i<passwordFragments.size(); i++){
                getSupportFragmentManager().beginTransaction().remove(passwordFragments.get(i)).commit();
            }
            //Обнуляем
            passwordFragments.clear();
            int fragmentsCount = 0;

            database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

            String whereClause = "isRemoved = ?";
            String[] whereArgs = {"0"};

            Cursor cursor = database.query(systemFolderName, null, whereClause, whereArgs, null, null, null);

            if (cursor.moveToFirst()) {

                String title = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
                String description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                int passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

                passwordFragments.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, systemFolderName));
                getSupportFragmentManager().beginTransaction().add(R.id.passwords_container, passwordFragments.get(fragmentsCount), "Password").commit();
                fragmentsCount++;
                while (cursor.moveToNext()) {

                    title = (FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key));
                    description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                    passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

                    passwordFragments.add(PasswordFragment.newInstance(title, description, 0, passwordId, fullFilePath, systemFolderName));
                    getSupportFragmentManager().beginTransaction().add(R.id.passwords_container, passwordFragments.get(fragmentsCount), "Password").commit();
                    fragmentsCount++;
                }
            }

            cursor.close();
            database.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.floatingActionButtonCreatePassword:
                Intent intent = new Intent(FolderActivity.this, CreatePasswordActivity.class);
                intent.putExtra("KEY", key);
                intent.putExtra("fullFilePath", fullFilePath);
                intent.putExtra("systemFolderName", systemFolderName);
                startActivityForResult(intent, ADD_PASS_AFTER_CREATING);
                break;
        }
    }
}
