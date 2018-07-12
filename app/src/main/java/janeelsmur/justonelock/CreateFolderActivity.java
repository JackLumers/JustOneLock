package janeelsmur.justonelock;

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import janeelsmur.justonelock.utilites.DBTableHelper;

public class CreateFolderActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher{

    //Сохраненное значение
    private String fullFilePath;

    private EditText folderName;
    private FloatingActionButton createFloatingActionButton;
    private ImageButton changeIconButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_creating);

        fullFilePath = getIntent().getExtras().getString("fullFilePath");

        Toolbar toolbar = (Toolbar) findViewById(R.id.creationFolderToolbar);
        toolbar.setTitle(R.string.folder_creating);
        setSupportActionBar(toolbar);
        //Показ кнопки "назад"
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        folderName = (EditText) findViewById(R.id.folderNameEditText);
        createFloatingActionButton = (FloatingActionButton) findViewById(R.id.createFolderFAB);
        changeIconButton = (ImageButton) findViewById(R.id.changeFolderIconButton);
        //Установка слушателей для кнопок
        createFloatingActionButton.setOnClickListener(this);
        changeIconButton.setOnClickListener(this);

        folderName.addTextChangedListener(this);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!folderName.getText().toString().equals("")) createFloatingActionButton.show();
            else createFloatingActionButton.hide();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createFolderFAB:
                v.setOnClickListener(null);
                createFolder();
                setResult(RESULT_OK); //Для создания фрагмента папки
                finish();
                break;

            case R.id.changeFolderIconButton:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    //Создание папки
    private void createFolder() {
        if (!folderName.getText().toString().equals("")) {
            SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

            //Составление строки
            ContentValues cv = new ContentValues();
            cv.put(DBTableHelper.FOLDER_INDEXER_NAMES_FIELD, folderName.getText().toString());
            cv.put(DBTableHelper.FOLDER_ICON_CODE, 0);
            cv.put(DBTableHelper.FOLDER_IS_FAVORITE, false);
            cv.put(DBTableHelper.FOLDER_IS_REMOVED, false);

            //Индекс созданной папки
            int folderIndex = 1;
            Cursor cursor = database.query(DBTableHelper.FOLDER_INDEXER_TABLE, null, null, null, null ,null, null);
            if(cursor.moveToLast()){ //Если существует последняя строка
                folderIndex = cursor.getInt(cursor.getColumnIndex(DBTableHelper.KEY_ID)) + 1;
                Log.d("FOLDER INDEX", "Folder creating: " + folderIndex);
            }

            //Создание папки
            DBTableHelper.createFolderTable(database, "folder" + folderIndex, cv);
            database.close();
            cursor.close();
        }
    }
}
