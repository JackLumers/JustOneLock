package janeelsmur.justonelock;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;

import java.io.File;

public class VaultCreatingActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {


    //Передаваемые значения
    private String folderPath;
    private String fileName;
    private String fullFilePath;
    private byte[] key = new byte[64];
    //--------------------



    private MaterialEditText fileNameEditText;
    private MaterialEditText keyEditText;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vault_creating);

        //Тулбар
        Toolbar toolbar = (Toolbar) findViewById(R.id.creatingVaultToolbar);
        toolbar.setTitle(R.string.vault_creating);
        setSupportActionBar(toolbar);
        //Показ кнопки "назад"
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Путь к папке (для того, чтобы создать полный путь к файлу)
        folderPath = getIntent().getExtras().getString("folderPath");

        fileNameEditText = (MaterialEditText) findViewById(R.id.vaultNameMaterialEditText);
        keyEditText = (MaterialEditText) findViewById(R.id.passwordMaterialEditText);
        fab = (FloatingActionButton) findViewById(R.id.createDatabaseFAButton);

        //Включение всплывающей подсказки
        fileNameEditText.setFloatingLabel(2);
        keyEditText.setFloatingLabel(2);

        //Добавление валидатора
        fileNameEditText.addValidator(new RegexpValidator("Допустимы буквы, цифры и символ \"_\"", "[A-z\\dА-я]+"));

        fab.setOnClickListener(this);
        fileNameEditText.addTextChangedListener(this);
        keyEditText.addTextChangedListener(this);
    }

    //-----TEXT WATCHER-----
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) { //при изменении текста
        boolean isLoginCorrect = false;
        if (fileNameEditText.length()<=30 && fileNameEditText.length()>0 && fileNameEditText.validate()) { //Проверяем, валидное ли название файла

            //Инициализирование строк пути к файлу
            fileName = fileNameEditText.getText().toString();
            fullFilePath = folderPath + File.separator + fileName + ".jol";
            File file = new File(fullFilePath);

            //Проверка, существует ли уже такой файл
            if (!file.exists()) {
                isLoginCorrect = true;
            } else {
                fileNameEditText.setError("Файл с таким названием уже существует");
                isLoginCorrect = false;
            }
        }

        if(isLoginCorrect && !keyEditText.getText().toString().equals("")) fab.show();
            else fab.hide();
    }
    //-----------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.scale_window_fit_screen_from_small, R.anim.scale_window_bigger);
    }

    @Override
    public void onClick(View v) {
        v.setOnClickListener(null); //Для предотвращения повторного нажатия

        key = FileAlgorithms.SHA256(keyEditText.getText().toString()); //Ключ

        //Создание базы данных
        SQLiteDatabase database = openOrCreateDatabase(fullFilePath, MODE_PRIVATE, null);
        database.enableWriteAheadLogging();
        //Создание таблицы с контрольной строкой
        byte[] encryptedSum = FileAlgorithms.Encrypt(DBTableHelper.CHECK_SUM_TEXT.getBytes(), key);
        ContentValues cv = new ContentValues();
        cv.put(DBTableHelper.CHECK_SUM_FIELD, encryptedSum);

        //Создание таблицы контрольной суммы
        DBTableHelper.createCheckSumTable(database, cv);
        //Создание таблицы индексирования папок
        DBTableHelper.createFolderIndexerTable(database);
        //Создание таблицы с паролями без папок
        DBTableHelper.createPasswordsWithoutFolderTable(database);
        //Создание таблицы заметок
        DBTableHelper.createNotesTable(database);
        database.close();

        //Передача значений имени файла и пути к файлу и запуск активности
        Intent intent = new Intent(VaultCreatingActivity.this, EnteringActivity.class);
        intent.putExtra("fileName", fileName);
        intent.putExtra("fullFilePath", fullFilePath);
        setResult(RESULT_OK); //Для закрытия главной активности
        startActivity(intent);
        finish();

    }

}
