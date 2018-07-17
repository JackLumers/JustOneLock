package janeelsmur.justonelock;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;
import janeelsmur.justonelock.utilites.SharedPreferencesManager;

public class EnteringActivity extends AppCompatActivity implements View.OnClickListener {

    //* СОХРАНЕННЫЕ ЗНАЧЕНИЯ *//
    private String fullFilePath;
    private String fileName;
    private byte[] key = new byte[64];

    private InputMethodManager keyboard;
    private TextView enterButton, name_of_file;
    private TextView changeVaultButton;
    private EditText masterKey;
    private TextView onEnterText;
    private ImageView blackDote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entering);

        keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        enterButton = (TextView) findViewById(R.id.enter_button);
        name_of_file = (TextView) findViewById(R.id.name_of_file);
        changeVaultButton = (TextView) findViewById(R.id.change_vault_button);
        masterKey = (EditText) findViewById(R.id.master_key);
        onEnterText = (TextView) findViewById(R.id.onEnterText);
        blackDote = (ImageView) findViewById(R.id.blackDote);

        enterButton.setOnClickListener(this);
        changeVaultButton.setOnClickListener(this);

        fullFilePath = getIntent().getExtras().getString("fullFilePath");
        fileName = getIntent().getExtras().getString("fileName");
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        sharedPreferencesManager.setFilePath(fullFilePath);
        name_of_file.setText("Название хранилища: " + fileName);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void onClick(View view) {

        switch (view.getId()) {

            //Ввод ключа
            case R.id.enter_button:
                String enteredKey = masterKey.getText().toString();
                key = FileAlgorithms.SHA256(enteredKey);
                if (isCorrectKey(key, fullFilePath)) {
                    keyboard.hideSoftInputFromWindow(enterButton.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); //Убираем клавиатуру

                    masterKey.setText("");
                    masterKey.setVisibility(View.VISIBLE);

                    blackDote.startAnimation((AnimationUtils.loadAnimation(this, R.anim.opening)));


                    //Открытие хранилища
                    Intent intent = new Intent(this, VaultActivity.class);
                    intent.putExtra("KEY", key);
                    intent.putExtra("fullFilePath", fullFilePath);
                    intent.putExtra("fileName", fileName);
                    setResult(RESULT_OK); //Для закрытия главной активности
                    startActivity(intent);
                    finish();
                } else {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(200L);
                    masterKey.setText("");
                    onEnterText.setText(R.string.WrongPassword);
                    onEnterText.setTextColor(getResources().getColor(R.color.lightRed));
                    onEnterText.setVisibility(View.VISIBLE);
                    onEnterText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha));
                    blackDote.startAnimation((AnimationUtils.loadAnimation(this, R.anim.triggering)));
                    onEnterText.setVisibility(View.INVISIBLE);
                }
                break;

            //Смена хранилища
            case R.id.change_vault_button:
                keyboard.hideSoftInputFromWindow(changeVaultButton.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); //Убираем клавиатуру
                //Удаляем запись пути к файлу
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
                sharedPreferencesManager.setFilePathToNULL();

                Intent intent = new Intent(EnteringActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.scale_window_fit_screen_from_small, R.anim.scale_window_bigger);

        }
    }

    private boolean isCorrectKey(byte[] key, String fullFilePath){
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        Cursor cursor = database.query(DBTableHelper.CHECK_SUM_TABLE_NAME, null, null, null, null, null, null, null);
        cursor.moveToFirst();
        byte[] checkSum = cursor.getBlob(cursor.getColumnIndex(DBTableHelper.CHECK_SUM_FIELD));
        cursor.close(); database.close();

        return FileAlgorithms.DecryptInString(checkSum, key) != null && FileAlgorithms.DecryptInString(checkSum, key).equals(DBTableHelper.CHECK_SUM_TEXT);
    }

}
