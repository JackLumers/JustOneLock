package janeelsmur.justonelock.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SharedPreferencesManager {

    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context){
        sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
    }

    public void setFilePath(String fullFilePath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fullFilePath", fullFilePath);
        editor.commit();
    }

    public void setFilePathToNULL(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("fullFilePath");
        editor.commit();
    }

    public String getFilePath(){
        return sharedPreferences.getString("fullFilePath", null);
    }

    public void disableWelcomeScreen() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstAppLaunch", false);
        editor.commit();
    }

    public boolean isFirstAppLaunch(){
        return sharedPreferences.getBoolean("isFirstAppLaunch", true);
    }

}
