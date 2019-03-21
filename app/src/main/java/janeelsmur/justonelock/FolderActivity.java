package janeelsmur.justonelock;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import janeelsmur.justonelock.adapters.PasswordAdapter;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.listeners.FragmentsMassagesListener;
import janeelsmur.justonelock.objects.Password;
import janeelsmur.justonelock.utilities.DBTableHelper;
import janeelsmur.justonelock.utilities.EncryptionAlgorithms;

import java.util.ArrayList;

public class FolderActivity extends AppCompatActivity implements View.OnClickListener, FragmentsMassagesListener {

    private final String TAG = "FolderActivity";
    private final int ADD_PASS_AFTER_CREATING = 1;

    /* СОХРАНЕННЫЕ ДАННЫЕ */
    private String fullFilePath;
    private String systemFolderName;
    private String folderName;
    private int folderId;
    private byte[] key;

    private RecyclerView passwordsRecycleView;
    private PasswordAdapter Adapter;

    private ArrayList<Password> passwordFragments = new ArrayList<>();

    private Menu globalMenu; // Будет нужно в будущем для динамического обновления кнопок на тулбаре

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        passwordFragments.toArray();

        /* СОХРАНЕННЫЕ ДАННЫЕ */
        fullFilePath = getIntent().getExtras().getString("fullFilePath");  //Полный путь к хранилищу
        systemFolderName = getIntent().getExtras().getString("systemFolderName"); //Системное название папки
        folderName = getIntent().getExtras().getString("folderName"); //Название папки для тулбара
        key = getIntent().getExtras().getByteArray("KEY"); //Ключ
        folderId = getIntent().getExtras().getInt("folderId"); //Id папки в таблице индексирования. Метка удаления производится по id.

        Toolbar toolbar = findViewById(R.id.folderToolbar);
        toolbar.setTitle(folderName);
        setSupportActionBar(toolbar);
        //Показ кнопки "назад"
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        passwordsRecycleView = findViewById(R.id.folder_recyclerview);
        passwordsRecycleView.setLayoutManager(new LinearLayoutManager(this));
        Adapter = new PasswordAdapter(passwordFragments, fullFilePath, key);
        passwordsRecycleView.setAdapter(Adapter);

        FloatingActionButton fab = findViewById(R.id.floatingActionButtonCreatePassword);
        fab.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        passwordFragments.clear();
        reloadPasswordsFromDatabase();
        Adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        globalMenu = menu;
        menu.getItem(2).setVisible(false);
        menu.getItem(3).setVisible(true);
        return true;
    }

    @Override
    public void onNotificationTaken(int notification) {
        switch (notification) {
            case FragmentsMassagesListener.FINISH:
                finish();
                break;

            case FragmentsMassagesListener.DATA_CHANGED:
                passwordFragments.clear();
                reloadPasswordsFromDatabase();
                Adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                DeleteDialog deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_FOLDER, folderId, fullFilePath, true);
                deleteDialog.show(getSupportFragmentManager(), "deleteDialog");
                break;

            case R.id.action_edit:
                break;

            default:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void reloadPasswordsFromDatabase() {
        //Очищаем список
        passwordFragments.clear();

        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        String whereClause = "isRemoved = ?";
        String[] whereArgs = {"0"};

        Cursor cursor = database.query(systemFolderName, null, whereClause, whereArgs, null, null, null);

        if (cursor.moveToFirst()) {

            String title = EncryptionAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key);
            String description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
            int passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

            passwordFragments.add(new Password(title, description, 0, passwordId, fullFilePath, systemFolderName));
            while (cursor.moveToNext()) {

                title = (EncryptionAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_SERVICE)), key));
                description = cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION));
                passwordId = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID));

                passwordFragments.add(new Password(title, description, 0, passwordId, fullFilePath, systemFolderName));
            }
        }

        cursor.close();
        database.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
