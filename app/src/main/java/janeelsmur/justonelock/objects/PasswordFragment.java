package janeelsmur.justonelock.objects;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import janeelsmur.justonelock.PasswordActivity;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;

public class PasswordFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    //*СОХРАНЕННЫЙ КЛЮЧ*//
    //byte[] key; в onClick

    //Значения
    private String title;
    private String description;
    private String fullFilePath;
    //Системное название папки. Может быть null, если пароль был прогружен из таблицы несгруппированных паролей.
    private String systemFolderName = null;
    private int passwordId;

    private TextView titleTextView;
    private TextView descriptionTextView;

    private RelativeLayout openPasswordActivityButton;
    private ImageView copyLoginButton;
    private FrameLayout copyPasswordButton;

    private DeleteDialog deleteDialog;

    private Toast copyToast;

    /**
     * @param title - заголовок пароля
     * @param description - описание пароля
     * @param passwordIconResID - id иконки пароля
     * @param passwordId - id пароля в базе данных
     * @param fullFilePath - полный путь к файлу базы данных
     * @param systemFolderName - системное название папки (по типу folder1). !!! Может быть названием таблицы с паролями без папок !!!
     */
    public static PasswordFragment newInstance(String title, String description, int passwordIconResID, int passwordId, String fullFilePath, String systemFolderName) {
        Bundle args = new Bundle();
        PasswordFragment fragment = new PasswordFragment();

        args.putString("fullFilePath", fullFilePath);
        args.putInt("passwordIconResID", passwordIconResID);
        args.putString("title", title);
        args.putString("description", description);
        args.putString("systemFolderName", systemFolderName);
        args.putInt("passwordId", passwordId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = getArguments().getString("title");
        description = getArguments().getString("description");
        fullFilePath = getArguments().getString("fullFilePath");
        systemFolderName = getArguments().getString("systemFolderName");
        passwordId = getArguments().getInt("passwordId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, null);

        copyToast = Toast.makeText(getActivity(), null, Toast.LENGTH_SHORT);

        titleTextView = view.findViewById(R.id.serviceNameTextView);
        descriptionTextView = view.findViewById(R.id.passwordPreviewTextView);
        titleTextView.setText(title);
        descriptionTextView.setText(description);

        openPasswordActivityButton = view.findViewById(R.id.openPasswordButtonLinLayout);
        copyLoginButton = view.findViewById(R.id.copyLoginImageButton);
        copyPasswordButton = view.findViewById(R.id.copyPasswordButton);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        openPasswordActivityButton.setOnClickListener(this);
        openPasswordActivityButton.setOnLongClickListener(this);
        copyPasswordButton.setOnClickListener(this);
        copyPasswordButton.setOnLongClickListener(this);
        copyLoginButton.setOnClickListener(this);
        copyLoginButton.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.copyLoginImageButton:
                copy(0);
                return true;

            case R.id.copyPasswordButton:
                copy(1);
                return true;

            case R.id.openPasswordButtonLinLayout:
                showDeleteDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.copyLoginImageButton:
                try {
                    copy(0);
                    copyToast.setText(R.string.loginCopy);
                    copyToast.show();
                } catch (Exception e){
                    break;
                }
                break;

            case R.id.copyPasswordButton:
                try {
                    copy(1);
                    copyToast.setText(R.string.passwordCopy);
                    copyToast.show();
                } catch (Exception e){
                    break;
                }
                break;

            case R.id.openPasswordButtonLinLayout:
                v.setOnClickListener(null);
                Intent intent = new Intent(getContext(), PasswordActivity.class);
                intent.putExtra("KEY", getActivity().getIntent().getExtras().getByteArray("KEY"));
                intent.putExtra("systemFolderName", systemFolderName);
                intent.putExtra("fullFilePath", fullFilePath);
                intent.putExtra("passwordId", passwordId);
                intent.putExtra("title", title);
                startActivity(intent);
                break;
        }
    }

    private void showDeleteDialog(){
        deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_PASSWORD, fullFilePath, systemFolderName, passwordId, false);
        deleteDialog.show(getFragmentManager(), "deleteDialog");
    }

    /** Копирует объект
     *
     * @param object:
     *               0 - Логин
     *               1 - Пароль
     */
    private void copy(int object){
        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

        String whereClause = DBTableHelper.KEY_ID + " = ?";
        String[] whereArgs = {String.valueOf(passwordId)};
        Cursor cursor = database.query(systemFolderName, null, whereClause, whereArgs, null, null, null);
        cursor.moveToFirst();

        byte[] key = getActivity().getIntent().getExtras().getByteArray("KEY");
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        switch (object) {
            case 1:
                String password = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_LOGIN)), key);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("password", password));
                cursor.close();
                database.close();
                break;

            case 0:
                String login = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_PASSWORD)), key);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("login", login));
                cursor.close();
                database.close();
                break;
        }
    }
}
