package janeelsmur.justonelock.dialogs;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.NotificationListener;

public class DeleteDialog extends DialogFragment implements View.OnClickListener {

    public final static int OBJECT_PASSWORD = 0;
    public final static int OBJECT_FOLDER = 1;
    public final static int OBJECT_NOTE = 2;

    private int passwordId;
    private int noteId;
    private String systemFolderName;
    private int folderId;
    private String fullFilePath;
    private int object;
    private boolean isCalledFromActivity;

    private NotificationListener notificationListener;

    /** Для удаления пароля */
    public static DeleteDialog newInstance(int object, String fullFilePath, String systemFolderName, int passwordId, boolean isCalledFromActivity) {
        Bundle args = new Bundle();

        args.putBoolean("isCalledFromActivity", isCalledFromActivity);
        args.putString("fullFilePath", fullFilePath);
        args.putString("systemFolderName", systemFolderName);
        args.putInt("passwordId", passwordId);
        args.putInt("object", object);

        DeleteDialog fragment = new DeleteDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /** Для удаления папки */
    public static DeleteDialog newInstance(int object, int folderId, String fullFilePath, boolean isCalledFromActivity) {
        Bundle args = new Bundle();

        args.putBoolean("isCalledFromActivity", isCalledFromActivity);
        args.putString("fullFilePath", fullFilePath);
        args.putInt("folderId", folderId);
        args.putInt("object", object);

        DeleteDialog fragment = new DeleteDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /** Для удаления заметки */
    public static DeleteDialog newInstance(int object, String fullFilePath, int noteId, boolean isCalledFromActivity){
        Bundle args = new Bundle();

        args.putBoolean("isCalledFromActivity", isCalledFromActivity);
        args.putString("fullFilePath", fullFilePath);
        args.putInt("noteId", noteId);
        args.putInt("object", object);

        DeleteDialog fragment = new DeleteDialog();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);

        object = getArguments().getInt("object");
        isCalledFromActivity = getArguments().getBoolean("isCalledFromActivity");

        notificationListener = (NotificationListener) getActivity();

        switch (object){
            case OBJECT_FOLDER:
                folderId = getArguments().getInt("folderId");
                fullFilePath = getArguments().getString("fullFilePath");
                break;

            case OBJECT_PASSWORD:
                fullFilePath = getArguments().getString("fullFilePath");
                systemFolderName = getArguments().getString("systemFolderName");
                passwordId = getArguments().getInt("passwordId");
                break;

            case OBJECT_NOTE:
                fullFilePath = getArguments().getString("fullFilePath");
                noteId = getArguments().getInt("noteId");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete, null);

        TextView text = view.findViewById(R.id.deleteTextView);
        TextView declineButton = view.findViewById(R.id.declineButton);
        TextView okButton = view.findViewById(R.id.okButton);

        if(object == OBJECT_FOLDER) text.setText(R.string.delete_folder);

        declineButton.setOnClickListener(this);
        okButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
            switch (v.getId()) {

                case R.id.declineButton:
                    dismiss();
                    break;

                case R.id.okButton:
                    if (object == OBJECT_PASSWORD) {
                        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
                        DBTableHelper.markPasswordDeleted(database, systemFolderName, passwordId, 1);
                        database.close();
                        if (isCalledFromActivity) { //Если папка удаляется из активности самой папки, то активность финишировать
                            notificationListener.onNotificationTaken(NotificationListener.FINISH);
                        }
                        else { //Иначе обновить объекты вью
                            notificationListener.onNotificationTaken(NotificationListener.DATA_CHANGED);
                            dismiss();
                        }
                    } else if (object == OBJECT_FOLDER) {
                        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
                        DBTableHelper.markFolderDeleted(database, folderId, 1);
                        database.close();
                        if (isCalledFromActivity) notificationListener.onNotificationTaken(NotificationListener.FINISH);
                        else notificationListener.onNotificationTaken(NotificationListener.DATA_CHANGED);
                        dismiss();
                    } else if (object == OBJECT_NOTE) {
                        SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
                        DBTableHelper.markNoteDeleted(database, noteId, 1);
                        database.close();
                        if (isCalledFromActivity) notificationListener.onNotificationTaken(NotificationListener.FINISH);
                        else notificationListener.onNotificationTaken(NotificationListener.DATA_CHANGED);
                        dismiss();
                    }
            }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
