package janeelsmur.justonelock;

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.utilites.NotificationListener;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener, NotificationListener {
    //Сохраненное значение
    private String fullFilePath;
    private byte[] key;
    private int noteId;

    private EditText noteText;
    private FloatingActionButton fab;

    private DeleteDialog deleteDialog;

    //Меню тулбара
    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        fullFilePath = getIntent().getExtras().getString("fullFilePath");
        key = getIntent().getExtras().getByteArray("KEY");
        noteId = getIntent().getExtras().getInt("noteId");

        Toolbar toolbar = (Toolbar) findViewById(R.id.noteToolbar);
        toolbar.setTitle("Заметка");
        setSupportActionBar(toolbar);
        //Показ кнопки "назад"
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteText = (EditText) findViewById(R.id.NoteText);
        fab = findViewById(R.id.createNoteFAB);

        //Если есть id, значит это уже созданная заметка. Загружаем текст
        if(noteId != 0) {
            SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
            String whereClause = DBTableHelper.KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(noteId)};
            Cursor cursor = database.query(DBTableHelper.NOTES_TABLE, null, whereClause, whereArgs, null, null, null);
            cursor.moveToFirst();
            byte[] encryptedText = cursor.getBlob(cursor.getColumnIndex(DBTableHelper.NOTE_TEXT));

            cursor.close();
            database.close();
            noteText.setText(FileAlgorithms.DecryptInString(encryptedText, key));
        }

        //Установка слушателей для кнопок
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createNoteFAB:
                v.setOnClickListener(null); //Для предотвращения повторного нажатия
                if(noteId != 0){
                    saveNewData();
                    finish();
                    break;
                } else {
                    createNote();
                    finish();
                    break;
                }
        }
    }

    private void createNote() {
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        //Зашифрованный текст
        byte[] encryptedText = FileAlgorithms.Encrypt(noteText.getText().toString().getBytes(), key);

        ContentValues cv = new ContentValues();
        cv.put(DBTableHelper.NOTE_TEXT, encryptedText);
        cv.put(DBTableHelper.NOTE_IS_REMOVED, 0);
        cv.put(DBTableHelper.NOTE_CREATING_DATETIME, System.currentTimeMillis());
        cv.put(DBTableHelper.NOTE_CHANGING_DATETIME, System.currentTimeMillis());

        DBTableHelper.createNote(database, cv);
        database.close();
    }

    private void saveNewData(){
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.NOTES_TABLE, null, DBTableHelper.KEY_ID + " = ?", new String[]{String.valueOf(noteId)}, null, null, null);
        cursor.moveToFirst();

        byte[] encryptedText = FileAlgorithms.Encrypt(noteText.getText().toString().getBytes(), key);

        ContentValues cv = new ContentValues();
        cv.put(DBTableHelper.KEY_ID, noteId);
        cv.put(DBTableHelper.NOTE_TEXT, encryptedText);
        cv.put(DBTableHelper.NOTE_CHANGING_DATETIME, System.currentTimeMillis());
        cv.put(DBTableHelper.NOTE_IS_REMOVED, 0);
        cv.put(DBTableHelper.NOTE_CREATING_DATETIME, cursor.getString(cursor.getColumnIndex(DBTableHelper.NOTE_CREATING_DATETIME)));

        database.replace(DBTableHelper.NOTES_TABLE, null, cv);

        cursor.close();
        database.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        this.menu = menu;
        if(noteId != 0) menu.getItem(3).setVisible(true);
        return true;
    }

    @Override
    public void onNotificationTaken(int notification) {
        switch (notification){
            case NotificationListener.FINISH:
                finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_remove:
                deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_NOTE, fullFilePath, noteId, true);
                deleteDialog.show(getSupportFragmentManager(), "deleteNote");
                break;

            default:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
