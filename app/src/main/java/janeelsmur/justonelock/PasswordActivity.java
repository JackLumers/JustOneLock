package janeelsmur.justonelock;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.*;
import com.rengwuxian.materialedittext.MaterialEditText;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.utilites.NotificationListener;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener, NotificationListener, CompoundButton.OnCheckedChangeListener{

    private final int EDITING_MODE = 1;
    private final int WATCHING_MODE = 0;

    //*СОХРАНЕННЫЕ ЗНАЧЕНИЯ*//
    private byte[] key;
    private String fullFilePath;
    private String systemFolderName;
    private String title;
    private int passwordId;

    private Toolbar toolbar;
    private FloatingActionButton saveNewPassDataFab;
    private MaterialEditText titleInEditMode;
    private MaterialEditText loginTextView;
    private MaterialEditText passwordTextView;
    private MaterialEditText descriptionTextView;
    private CheckBox inFavorites;
    private ImageView hidePasswordButton;
    private ImageView copyLogin;
    private FrameLayout copyPassword;

    private ClipboardManager clipboardManager;

    private Toast copyToast;

    //Изменил ли пользователь избранные
    private boolean isFavoritesStateChanged = false;

    //Сохранение меню после создания
    private Menu globalMenu;

    //Диалоговое окно удаления
    private DeleteDialog deleteDialog;

    //Старые значение, которые выставляются при выходе из режима редактирования
    private String savedLogin;
    private String savedPassword;
    private String savedTitle;
    private String savedDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        copyToast = Toast.makeText(this, null, Toast.LENGTH_SHORT);

        //Тулбар
        title = getIntent().getExtras().getString("title");
        toolbar = (Toolbar) findViewById(R.id.passwordToolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        //Показ кнопки "назад"
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Созраненные значения
        key = getIntent().getExtras().getByteArray("KEY");
        fullFilePath = getIntent().getExtras().getString("fullFilePath");
        systemFolderName = getIntent().getExtras().getString("systemFolderName");
        passwordId = getIntent().getExtras().getInt("passwordId");

        saveNewPassDataFab = (FloatingActionButton) findViewById(R.id.saveNewPassDataFab);
        hidePasswordButton = (ImageView) findViewById(R.id.hidePasswordBtn);
        loginTextView = (MaterialEditText) findViewById(R.id.loginTextView);
        passwordTextView = (MaterialEditText) findViewById(R.id.passwordTextView);
        descriptionTextView = (MaterialEditText) findViewById(R.id.descriptionTextView);
        inFavorites = (CheckBox) findViewById(R.id.checkBox);
        titleInEditMode = (MaterialEditText) findViewById(R.id.title_in_edit_mode);
        copyLogin = findViewById(R.id.loginImageView);
        copyPassword = findViewById(R.id.passwordImageViewBtn);

        titleInEditMode.setText(title);

        loadData();

        copyLogin.setOnClickListener(this);
        copyPassword.setOnClickListener(this);
        passwordTextView.setOnClickListener(this);
        loginTextView.setOnClickListener(this);
        hidePasswordButton.setOnClickListener(this);
        saveNewPassDataFab.setOnClickListener(this);
        inFavorites.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        passwordTextView.setInputType(129);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.passwordImageViewBtn:
                try {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("password", passwordTextView.getText().toString()));
                    copyToast.setText(R.string.passwordCopy);
                    copyToast.show();
                } catch (Exception e){
                    Log.d("!", "onClick: " + e.getLocalizedMessage());
                }
                break;

            case R.id.loginImageView:
                try {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("login", loginTextView.getText().toString()));
                    copyToast.setText(R.string.loginCopy);
                    copyToast.show();
                } catch (Exception e){
                    Log.d("!", "onClick: " + e.getLocalizedMessage());
                }
                break;

            case R.id.saveNewPassDataFab:
                saveNewData();
                saveOldValues();
                isFavoritesStateChanged = false;
                setMode(WATCHING_MODE);
                break;

            case R.id.hidePasswordBtn:
                switch (passwordTextView.getInputType()) {

                    case 129:
                        hidePasswordButton.setImageResource(R.drawable.ic_eye_off);
                        passwordTextView.setInputType(144);
                        break;

                    case 144:
                        hidePasswordButton.setImageResource(R.drawable.ic_eye);
                        passwordTextView.setInputType(129);
                        break;
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d("Password", "onCheckedChanged: " + buttonView.isChecked());
        saveNewPassDataFab.show();
        isFavoritesStateChanged = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        globalMenu = menu;
        menu.getItem(3).setVisible(true);
        menu.getItem(2).setVisible(true);
        return true;
    }

    @Override
    public void onNotificationTaken(int notification) {
        switch (notification) {
            case NotificationListener.FINISH:
                finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //Вход в режим редактирования
            case R.id.action_edit:
                setMode(EDITING_MODE);
                break;

            case R.id.action_remove:
                deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_PASSWORD, fullFilePath, systemFolderName, passwordId, true);
                deleteDialog.show(getSupportFragmentManager(), "deleteDialog");
                break;

            default:
                if(titleInEditMode.getVisibility() == View.VISIBLE)
                    setMode(WATCHING_MODE);
                else finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Загрузка данных из БД (с расшифровкой заголовка, логина и пароля)
     * Если пароль не сгруппирован, то он прогрузится из таблицы несгруппированных паролей
     */
    private void loadData() {
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        String whereClause = DBTableHelper.KEY_ID + " = ?";
        String[] whereArgs = {String.valueOf(passwordId)};
        Cursor cursor = database.query(systemFolderName, null, whereClause, whereArgs, null, null, null);
        cursor.moveToFirst();

        if (cursor.getInt(cursor.getColumnIndex(DBTableHelper.PASS_IS_FAVORITE)) == 1) inFavorites.setChecked(true);
        loginTextView.setText(FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_LOGIN)), key));
        passwordTextView.setText(FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_PASSWORD)), key));
        descriptionTextView.setText(cursor.getString(cursor.getColumnIndex(DBTableHelper.PASS_DESCRIPTION)));

        cursor.close();
        database.close();
    }

    //Режим редактирования
    private void setMode(int mode) {
        switch (mode) {
            case EDITING_MODE:
                saveOldValues();
                toolbar.setTitle("Редактирование");
                globalMenu.getItem(2).setVisible(false);
                globalMenu.getItem(3).setVisible(false);
                titleInEditMode.setVisibility(View.VISIBLE);
                loginTextView.setEnabled(true);
                passwordTextView.setEnabled(true);
                descriptionTextView.setEnabled(true);
                saveNewPassDataFab.show();
                break;

            case WATCHING_MODE:
                returnOldValues(savedLogin, savedPassword, savedDescription, savedTitle); //Восстановление старых сначений
                globalMenu.getItem(3).setVisible(true);
                globalMenu.getItem(2).setVisible(true);
                titleInEditMode.setVisibility(View.GONE);
                loginTextView.setEnabled(false);
                passwordTextView.setEnabled(false);
                descriptionTextView.setEnabled(false);
                if(!isFavoritesStateChanged) saveNewPassDataFab.hide();
                toolbar.setTitle(title);
                break;
        }
    }

    /** Восстанавливает старые значения, если пользователь вышел из режима редактирования не задав новые */
    private void returnOldValues(String login, String password, String description, String title){
        descriptionTextView.setText(description);
        loginTextView.setText(login);
        passwordTextView.setText(password);
        titleInEditMode.setText(title);
    }

    /** Сохраняет старые значения */
    private void saveOldValues(){
        savedTitle = titleInEditMode.getText().toString();
        savedDescription = descriptionTextView.getText().toString();
        savedLogin = loginTextView.getText().toString();
        savedPassword = passwordTextView.getText().toString();
    }

    /**
     * Сохранить новые значения пароля в папку (если он был в папке).
     * Если пароль не был сгруппирован, то он сохранится в таблицу для несгруппированных паролей
     */
    private void saveNewData() {
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        //Составление строки
        title = titleInEditMode.getText().toString(); //Обновление заголовка для тулбара
        ContentValues cv = new ContentValues();
        cv.put(DBTableHelper.KEY_ID, passwordId);
        cv.put(DBTableHelper.PASS_IS_FAVORITE, inFavorites.isChecked());
        cv.put(DBTableHelper.PASS_IMAGE_CODE, "MUST BE RESOURCE CODE");
        cv.put(DBTableHelper.PASS_DESCRIPTION, descriptionTextView.getText().toString());
        cv.put(DBTableHelper.PASS_IS_REMOVED, false);

        //Зашифрованные логин и пароль
        byte[] encryptedService = FileAlgorithms.Encrypt(titleInEditMode.getText().toString().getBytes(), key);
        byte[] encryptedLogin = FileAlgorithms.Encrypt(loginTextView.getText().toString().getBytes(), key);
        byte[] encryptedPassword = FileAlgorithms.Encrypt(passwordTextView.getText().toString().getBytes(), key);
        cv.put(DBTableHelper.PASS_SERVICE, encryptedService);
        cv.put(DBTableHelper.PASS_LOGIN, encryptedLogin);
        cv.put(DBTableHelper.PASS_PASSWORD, encryptedPassword);
        cv.put(DBTableHelper.PASS_CHANGING_DATETIME, System.currentTimeMillis());

        //Обновление записи
        database.replace(systemFolderName, null, cv);

        database.close();
    }

    @Override
    public void onBackPressed() {
        if(titleInEditMode.getVisibility() == View.VISIBLE)
            setMode(WATCHING_MODE);
        else finish();
    }
}
