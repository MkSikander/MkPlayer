package com.mbytes.mkplayer.Utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.media3.common.C;

public class Preferences {

    private static final String MYPREF = "mypref";
    private SharedPreferences preferences;

    public Preferences(Context context) {
        preferences = context.getSharedPreferences(MYPREF, MODE_PRIVATE);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public int getInt(String key){
        return preferences.getInt(key,0);
    }
    public void setInt(String key,int value){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(key, value);
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }
    public void setLong(String key,Long playbackPosition){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putLong(key,playbackPosition);
        editor.apply();
    }
    public Long getLong(String key){
        return preferences.getLong(key,0);
    }
    public boolean getBoolean(String key){
        return preferences.getBoolean(key,false);
    }
    public void setBoolean(String key,boolean value){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    public void setLastVideos(String key,String key1,int position,String json){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(key1,position);
        editor.putString(key,json);
        editor.apply();
    }

    public String getVideoList(String key){
        return preferences.getString(key,"");
    }

    public int getVideoPosition(String key){
        return preferences.getInt(key, C.INDEX_UNSET);
    }

    public void setFolderSortPref(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public String getFolderSortPref(String key) {
        return preferences.getString(key, "abc");
    }


    public boolean contains(String sort) {
        return !preferences.contains(sort);
    }

    public String getVideoSortPref(String key) {
        return preferences.getString(key,"abc");
    }

    public void setVideoSortPref(String key, String SortOption) {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(key, SortOption);
        editor.apply();
    }


}
