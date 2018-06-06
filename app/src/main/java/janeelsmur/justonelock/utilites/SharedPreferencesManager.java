package janeelsmur.justonelock.utilites;

import android.content.Context;
import android.content.SharedPreferences;

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

//
//    /** Изменяет состояние флажка измененных избранных на true **/
//    public void setFavoritesChanged(@Nullable Set){
//        Set<Integer> addedId
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("isFavoritesChanged", true);
//        editor.commit();
//    }
//
//    /** Устанавливает флажек измененных избранных на false **/
//    public void setFavoritesChangesCommited(){
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove("isFavoritesChanged");
//    }
//
//    /** Проверяет, изменены ли избранные **/
//    public boolean isFavoritesChanged(){
//        return sharedPreferences.getBoolean("isFavoritesChanged", false);
//    }

    public void disableWelcomeScreen() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstAppLaunch", false);
        editor.commit();
    }


    public boolean isFirstAppLaunch(){
        return sharedPreferences.getBoolean("isFirstAppLaunch", true);
    }
}
