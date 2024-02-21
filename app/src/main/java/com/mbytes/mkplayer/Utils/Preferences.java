package com.mbytes.mkplayer.Utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.media3.common.C;

public class Preferences {

    private static final String MYPREF = "mypref";
    private final SharedPreferences preferences;

    public Preferences(Context context) {
        preferences = context.getSharedPreferences(MYPREF, MODE_PRIVATE);
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
    public void setSeekGesture(boolean value){
        String key="seek_gesture_setting";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public boolean getSeekGesture(){
        String key="seek_gesture_setting";
       return preferences.getBoolean(key,true);
    }
    public void setScrollGesture(boolean value){
        String key="scroll_gesture_setting";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public boolean getScrollGesture(){
        String key="scroll_gesture_setting";
        return preferences.getBoolean(key,true);
    }
    public void setZoomGesture(boolean value){
        String key="zoom_gesture_setting";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public boolean getZoomGesture(){
        String key="zoom_gesture_setting";
        return preferences.getBoolean(key,true);
    }
    public void setResumePref(boolean value){
        String key="resume_setting";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public boolean getResumePref(){
        String key="resume_setting";
        return preferences.getBoolean(key,true);
    }
    public void setFastSeekPref(boolean value){
        String key="fast_seek_setting";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public boolean getFastSeekPref(){
        String key="fast_seek_setting";
        return preferences.getBoolean(key,true);
    }
    public void setBrightnessPref(boolean value){
        String key="brightness_setting";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public boolean getBrightnessPref(){
        String key="brightness_setting";
        return preferences.getBoolean(key,true);
    }
    public void setAutoPlayPref(boolean value){
        String key="autoplay_setting";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public boolean getAutoPlayPref(){
        String key="autoplay_setting";
        return preferences.getBoolean(key,true);
    }

    public void setCurrentBrightnessPref(float value){
        String key="previous_brightness";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putFloat(key,value);
        editor.apply();
    }
    public float getPreviousBrightnessPref(){
        String key="previous_brightness";
        return preferences.getFloat(key,0f);
    }
    public void setDefaultPlaybackSpeed(int value){
        String key="default_playback_speed";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(key,value);
        editor.apply();
    }
    public int getDefaultPlaybackSpeed(){
        String key="default_playback_speed";
        return preferences.getInt(key,3);
    }
    public void setDefaultSeekSpeed(int value){
        String key="default_seek_speed";
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(key,value);
        editor.apply();
    }
    public int getDefaultSeekSpeed(){
        String key="default_seek_speed";
        return preferences.getInt(key,0);
    }


    public void setDefaultOrientation(int i) {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("def_orient",i);
        editor.apply();
    }

    public int getDefaultOrientation() {
        return preferences.getInt("def_orient",2);
    }


    public void setContrast(boolean value) {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("contrast",value);
        editor.apply();
    }
    public boolean getContrast(){
        return preferences.getBoolean("contrast",false);
    }

    public void setDynamicTheme(boolean value) {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("dynamic_theme",value);
        editor.apply();
    }
    public boolean getDynamicTheme(){
        return preferences.getBoolean("dynamic_theme",false);
    }

    public String getStoragePermission() {
        return preferences.getString("Allow","");
    }
    public void setStoragePermission(String value){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("Allow",value);
        editor.apply();
    }

    public void setDefaultTheme(int value) {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("DefTheme",value);
        editor.apply();
    }

    public int getDefaultTheme() {
        return preferences.getInt("DefTheme",-1);
    }

    public void setSelectedTheme(int i) {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("selected_theme",i);
        editor.apply();
    }
    public int getSelectedTheme(){
        return preferences.getInt("selected_theme",0);
    }

}
