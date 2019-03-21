package janeelsmur.justonelock;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import janeelsmur.justonelock.utilities.DBTableHelper;
import janeelsmur.justonelock.utilities.EncryptionAlgorithms;

public class CreatePasswordActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    //* СОХРАНЕННЫЕ ДАННЫЕ *//
    private String fullFilePath;
    private String systemFolderName;
    private byte[] key;

    private FloatingActionButton createButtonFab;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText loginEditText;
    private EditText passwordEditText;
    private ImageButton changeIcon;
    private CheckBox addInFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);

        Toolbar toolbar = (Toolbar) findViewById(R.id.createPasswordToolbar);
        toolbar.setTitle("Создание пароля");
        setSupportActionBar(toolbar);
        //Показ кнопки "назад"
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fullFilePath = getIntent().getExtras().getString("fullFilePath");
        systemFolderName = getIntent().getExtras().getString("systemFolderName");
        key = getIntent().getExtras().getByteArray("KEY");

        createButtonFab = (FloatingActionButton) findViewById(R.id.createPasswordFAB);
        changeIcon = (ImageButton) findViewById(R.id.changePasswordIconButton);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        loginEditText = (EditText) findViewById(R.id.loginEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        addInFavorites = (CheckBox) findViewById(R.id.addInFavAfterCreatingCheckBox);

        createButtonFab.setOnClickListener(this);
        changeIcon.setOnClickListener(this);

        titleEditText.addTextChangedListener(this);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!titleEditText.getText().toString().equals("")) createButtonFab.show();
            else createButtonFab.hide();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.changePasswordIconButton: break;

            case R.id.createPasswordFAB:
                if(systemFolderName != null) createPasswordInFolder();
                else createPassword();
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    //Создание пароля в папке
    private void createPasswordInFolder(){
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        //Зашифрованные логин и пароль
        byte[] encryptedService = EncryptionAlgorithms.Encrypt(titleEditText.getText().toString().getBytes(), key);
        byte[] encryptedLogin = EncryptionAlgorithms.Encrypt(loginEditText.getText().toString().getBytes(), key);
        byte[] encryptedPassword = EncryptionAlgorithms.Encrypt(passwordEditText.getText().toString().getBytes(), key);

        //Составление строки
        ContentValues cv = new ContentValues();
        cv.put(DBTableHelper.PASS_IS_FAVORITE, addInFavorites.isChecked());
        cv.put(DBTableHelper.PASS_IMAGE_CODE, "MUST BE RESOURCE CODE");
        cv.put(DBTableHelper.PASS_DESCRIPTION, descriptionEditText.getText().toString());
        cv.put(DBTableHelper.PASS_IS_REMOVED, false);
        cv.put(DBTableHelper.PASS_SERVICE, encryptedService);
        cv.put(DBTableHelper.PASS_LOGIN, encryptedLogin);
        cv.put(DBTableHelper.PASS_PASSWORD, encryptedPassword);
        cv.put(DBTableHelper.PASS_CREATING_DATETIME, System.currentTimeMillis());
        cv.put(DBTableHelper.PASS_CHANGING_DATETIME, System.currentTimeMillis());

        DBTableHelper.createPasswordInFolder(database, systemFolderName, cv);
        database.close();
    }

    //Создание пароля без папки
    private void createPassword(){
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        //Зашифрованные логин и пароль
        byte[] encryptedService = EncryptionAlgorithms.Encrypt(titleEditText.getText().toString().getBytes(), key);
        byte[] encryptedLogin = EncryptionAlgorithms.Encrypt(loginEditText.getText().toString().getBytes(), key);
        byte[] encryptedPassword = EncryptionAlgorithms.Encrypt(passwordEditText.getText().toString().getBytes(), key);

        //Составление строки
        ContentValues cv = new ContentValues();
        cv.put(DBTableHelper.PASS_IS_FAVORITE, addInFavorites.isChecked());
        cv.put(DBTableHelper.PASS_IMAGE_CODE, "MUST BE RESOURCE CODE");
        cv.put(DBTableHelper.PASS_DESCRIPTION, descriptionEditText.getText().toString());
        cv.put(DBTableHelper.PASS_IS_REMOVED, false);
        cv.put(DBTableHelper.PASS_SERVICE, encryptedService);
        cv.put(DBTableHelper.PASS_LOGIN, encryptedLogin);
        cv.put(DBTableHelper.PASS_PASSWORD, encryptedPassword);
        cv.put(DBTableHelper.PASS_CREATING_DATETIME, System.currentTimeMillis());
        cv.put(DBTableHelper.PASS_CHANGING_DATETIME, System.currentTimeMillis());

        DBTableHelper.createPassword(database, cv);
        database.close();
    }

    @Override
    public void onLowMemory() {
        onDestroy();
        super.onLowMemory();
    }
}
