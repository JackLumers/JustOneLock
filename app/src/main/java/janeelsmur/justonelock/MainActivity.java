package janeelsmur.justonelock;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import janeelsmur.justonelock.utilites.PathUtilite;
import janeelsmur.justonelock.utilites.SharedPreferencesManager;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Результирующий коды //
    private static final int GET_FILE_PATH_RESULT_CODE = 5;
    private final int EXTERNAL_STORAGE_PERMISSION_CODE = 2;
    private final int FINISH_ACTIVITY_CODE = 3;
    private final String WRITE_TO_SD_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    //Дефолтный путь к папке
    private final String folderPath = Environment.getExternalStorageDirectory() + File.separator + "JustOneLock Library";

    //Кнопки
    private RelativeLayout openExistingButton;
    private RelativeLayout createNewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);

        //Запрос на разрешение, если его нет
        if (!isPermissionGranted(WRITE_TO_SD_PERMISSION)) {
            requestPermission(WRITE_TO_SD_PERMISSION, EXTERNAL_STORAGE_PERMISSION_CODE);
        //Запрос на сохраненный путь к файлу. Если путь сохранен, то сразу открываем активность с вводом пароля
        } else if (sharedPreferencesManager.getFilePath() != null){
            //Передача значений имени файла и пути к файлу и запуск активности
            String fullFilePath = sharedPreferencesManager.getFilePath();
            openEnteringActivity(fullFilePath);
            finish();
        }

        openExistingButton = (RelativeLayout) findViewById(R.id.openExistingButton);
        createNewButton = (RelativeLayout) findViewById(R.id.createNewDataBaseButton);
        openExistingButton.setOnClickListener(this);
        createNewButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        openExistingButton.setOnClickListener(this);
        createNewButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            //Открытие существующего хранилища через проводник
            case R.id.openExistingButton:
                    openExistingButton.setOnClickListener(null);
                    createNewButton.setOnClickListener(null);
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, GET_FILE_PATH_RESULT_CODE);
                    overridePendingTransition(R.anim.scale_window_fit_screen, R.anim.scale_window_smaller);
                    break;

            //Создание нового хранилища
            case R.id.createNewDataBaseButton:
                    openExistingButton.setOnClickListener(null);
                    createNewButton.setOnClickListener(null);
                    Intent intent2 = new Intent(MainActivity.this, VaultCreatingActivity.class);
                    intent2.putExtra("folderPath", folderPath);
                    //Если мы создали новую базу, то завершаем эту активность
                    startActivityForResult(intent2, FINISH_ACTIVITY_CODE);
                    overridePendingTransition(R.anim.scale_window_fit_screen, R.anim.scale_window_smaller);
                    break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                //Реквест на разрешения
                case EXTERNAL_STORAGE_PERMISSION_CODE:
                    requestApplicationConfig(); break;

                //Реквест на путь к файлу
                case GET_FILE_PATH_RESULT_CODE:
                    Uri filePathURI = data.getData();
                    String fullFilePath = PathUtilite.getPath(this, filePathURI);
                    openEnteringActivity(fullFilePath); //Запуск активности ввода пароля с сохраненным путём к файлу
                    finish();
                    break;

                //Реквест на закрытие активности при создании новой базы данных
                case FINISH_ACTIVITY_CODE:
                    finish();
                    break;
            }
        }
    }

    private void openEnteringActivity (String fullFilePath) {
        Intent intent = new Intent(this, EnteringActivity.class);
        intent.putExtra("fullFilePath", fullFilePath);
        intent.putExtra("fileName", getFileNameFromFilePath(fullFilePath));
        startActivityForResult(intent, FINISH_ACTIVITY_CODE); //Для закрытия этой активности, когда пользователь откроет хранилище
    }

    private String getFileNameFromFilePath (String fullFilePath) {
        char[] fullPathArray = fullFilePath.toCharArray();
        StringBuilder fileName = new StringBuilder();
        int arrayLength = fullPathArray.length;

        int i = arrayLength-1;
        while (i>=0){
            if(fullPathArray[i] == File.separatorChar) break;
            i--;
        }

        i++;
        while (fullPathArray[i] != '.'){
            fileName.append(fullPathArray[i]);
            i++;
        }

        Log.d("FILE_NAME_FROM_OPENING", "getFileNameFromFilePath: " + fileName.toString());
        return fileName.toString();
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    } //Проверка на наличие разрешения

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permission}, requestCode);
    } //Запрос на разрешение

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getBaseContext(), (R.string.PermissionToWriteGranted), Toast.LENGTH_LONG).show();
            } else {
                showPermissionDialog(MainActivity.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showPermissionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = getResources().getString(R.string.app_name);
        builder.setTitle(title);
        builder.setMessage("Извините, но для работы нужно разрешение на запись и чтение данных. Дайте доступ к памяти в настройках.");

        String positiveText = "Настройки";
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppSettings();
            }
        }); // Кнопка открытия настроек

        String negativeText = "Выход";
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }); // Кнопка выхода

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        }); // Если пользователь закрыл окно

        AlertDialog dialog = builder.create();
        dialog.show();
    } //Создание диалогового окна

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, EXTERNAL_STORAGE_PERMISSION_CODE);
    } //Открытие настроек

    private void requestApplicationConfig() {
        if (isPermissionGranted(WRITE_TO_SD_PERMISSION)) {
            Toast.makeText(MainActivity.this, "Отлично, разрешения получены, можем работать :)", Toast.LENGTH_LONG).show();
        } else {
            requestPermission(WRITE_TO_SD_PERMISSION, EXTERNAL_STORAGE_PERMISSION_CODE);
        }
    }
}
