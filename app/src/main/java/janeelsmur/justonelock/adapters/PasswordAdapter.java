package janeelsmur.justonelock.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import janeelsmur.justonelock.CreatePasswordActivity;
import janeelsmur.justonelock.NoteActivity;
import janeelsmur.justonelock.PasswordActivity;
import janeelsmur.justonelock.R;
import janeelsmur.justonelock.dialogs.DeleteDialog;
import janeelsmur.justonelock.objects.Note;
import janeelsmur.justonelock.objects.Password;
import janeelsmur.justonelock.objects.PasswordFragment;
import janeelsmur.justonelock.utilites.DBTableHelper;
import janeelsmur.justonelock.utilites.FileAlgorithms;

/**
 * Created by Ильназ on 20.02.2018.
 */

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.MyViewHolder> {

    private ArrayList<Password> passwords;
    private Context context;
    private String fullFilePath;
    private byte[] key;
    private String title;
    private DeleteDialog deleteDialog;

    public PasswordAdapter(ArrayList<Password> passwords, String fullFilePath, byte[] key) {
        this.passwords = passwords;
        this.fullFilePath = fullFilePath;
        this.key = key;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_password, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.titleTextView.setText(passwords.get(position).title);
        holder.login.setText(passwords.get(position).description);
        holder.passwordId = passwords.get(position).passwordId;
        holder.systemFolderName=passwords.get(position).systemFolderName;

    }

    @Override
    public int getItemCount() {
        return (passwords == null) ? 0 : passwords.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView titleTextView, login;
        private int passwordId;
        private String systemFolderName;
        private Toast copyToast;
        private RelativeLayout openPasswordActivityButton;
        private ImageView copyLoginButton;
        private FrameLayout copyPasswordButton;

        MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            titleTextView = (TextView) view.findViewById(R.id.serviceNameTextView);
            login = view.findViewById(R.id.passwordPreviewTextView);
            copyToast = Toast.makeText(((AppCompatActivity) context), null, Toast.LENGTH_SHORT);

            openPasswordActivityButton = view.findViewById(R.id.openPasswordButtonLinLayout);
            copyLoginButton = view.findViewById(R.id.copyLoginImageButton);
            copyPasswordButton = view.findViewById(R.id.copyPasswordButton);
            openPasswordActivityButton.setOnClickListener(this);
            openPasswordActivityButton.setOnLongClickListener(this);
            copyPasswordButton.setOnClickListener(this);
            copyPasswordButton.setOnLongClickListener(this);
            copyLoginButton.setOnClickListener(this);
            copyLoginButton.setOnLongClickListener(this);


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
            Intent intent = new Intent(context, PasswordActivity.class);
            intent.putExtra("KEY", ((AppCompatActivity) context).getIntent().getExtras().getByteArray("KEY"));
            intent.putExtra("systemFolderName", systemFolderName);
            intent.putExtra("fullFilePath", fullFilePath);
            intent.putExtra("passwordId", passwordId);
            intent.putExtra("title", title);
            context.startActivity(intent);
            break;
            }
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
                    deleteDialog = DeleteDialog.newInstance(DeleteDialog.OBJECT_PASSWORD, fullFilePath, systemFolderName, passwordId, false);
                    deleteDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "deleteDialog");
                    return true;
            }
            notifyDataSetChanged();
            return false;

        }
        private void copy(int object){
            SQLiteDatabase database = SQLiteDatabase.openDatabase(fullFilePath, null, SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);

            String whereClause = DBTableHelper.KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(passwordId)};
            Cursor cursor = database.query(systemFolderName, null, whereClause, whereArgs, null, null, null);
            cursor.moveToFirst();

            byte[] key = ((AppCompatActivity) context).getIntent().getExtras().getByteArray("KEY");
            ClipboardManager clipboardManager = (ClipboardManager) (context).getSystemService(Context.CLIPBOARD_SERVICE);

            switch (object) {
                case 0:
                    String password = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_LOGIN)), key);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("password", password));
                    cursor.close();
                    database.close();
                    break;

                case 1:
                    String login = FileAlgorithms.DecryptInString(cursor.getBlob(cursor.getColumnIndex(DBTableHelper.PASS_PASSWORD)), key);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("login", login));
                    cursor.close();
                    database.close();
                    break;
            }
        }
    }



}
