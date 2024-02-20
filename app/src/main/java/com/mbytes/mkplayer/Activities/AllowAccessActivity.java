package com.mbytes.mkplayer.Activities;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;

public class AllowAccessActivity extends AppCompatActivity {

    public static final int STORAGE_PERMISSION = 1;
    public static final int STORAGE_PERMISSION_ABOVE10 = 123;
    public static final int REQUEST_PERMISSION_SETTING = 12;
    private static final int READ_VIDEO_PERMISSION_CODE = 100;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);
        TextView allow_btn = findViewById(R.id.allow_access);
        preferences=new Preferences(this);
        String value = preferences.getStoragePermission();
//        if(preferences.getDarkTheme()&& !preferences.getDefaultTheme()){
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else if (!preferences.getDarkTheme() && preferences.getDefaultTheme()) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        } else if (!preferences.getDarkTheme() && !preferences.getDefaultTheme()) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
        if (value.equals("OK")) {
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }
        allow_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @OptIn(markerClass = UnstableApi.class) @Override
            public void onClick(View v) {
                if (Util.SDK_INT < Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                        finish();
                    } else {
                        ActivityCompat.requestPermissions(AllowAccessActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
                    }
                } else {
                     if (Util.SDK_INT<Build.VERSION_CODES.TIRAMISU){
                        if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, READ_VIDEO_PERMISSION_CODE);
                        }}
                    if (Environment.isExternalStorageManager()) {
                        startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                        finish();
                    } else {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, STORAGE_PERMISSION_ABOVE10);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent,STORAGE_PERMISSION_ABOVE10);
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                String per = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                    boolean showRationale = shouldShowRequestPermissionRationale(per);
                    if (!showRationale) {
                        //user clicked on never ask again

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("App Permission")
                                .setMessage("For Playing videos, you must allow this app to access video files on your device"
                                        + "\n\n" + "Now follow the below steps" + "\n\n" +
                                        "Open Settings from below button" + "\n"
                                        + "Click on Permissions" + "\n" + "Allow access for storage")
                                .setPositiveButton("Open Settings", (dialog, which) -> {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
                                }).create().show();
                    } else {
                        ActivityCompat.requestPermissions(AllowAccessActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
                    }
                } else {
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    preferences.setStoragePermission("OK");
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSION_ABOVE10) {
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            preferences.setStoragePermission("OK");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }
    }
}