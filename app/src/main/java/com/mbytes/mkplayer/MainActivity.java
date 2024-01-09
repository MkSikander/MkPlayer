package com.mbytes.mkplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mbytes.mkplayer.Adapter.VideoFoldersAdapter;
import com.mbytes.mkplayer.Model.VideoFolder;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_ABOVE10 = 123;
    private RecyclerView foldersRecyclerview;
    private VideoFoldersAdapter adapter;
    ArrayList<String> FolderPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FolderPath = new ArrayList<>();
        setContentView(R.layout.activity_main);
        foldersRecyclerview = findViewById(R.id.folders_recyclerview);
        foldersRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        foldersRecyclerview.setHasFixedSize(true);

        if (isVideoFoldersEmpty()) {
            // Redirect to AllowAccessActivity
            redirectToAllowAccessActivity();
        } else {
            // Load video folders
            loadVideoFolders();
        }

    }



    private boolean isVideoFoldersEmpty() {
        List<VideoFolder> videoFolders = getVideoFolders();
        return videoFolders.isEmpty();
    }

    private void redirectToAllowAccessActivity() {
        // Update shared preference to "default"
        updateAllowAccessPreference("default");

        // Redirect to AllowAccessActivity
        Intent intent = new Intent(MainActivity.this, AllowAccessActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateAllowAccessPreference(String value) {
        SharedPreferences preferences = getSharedPreferences("AllowAccess", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Allow", value);
        editor.apply();
    }

    private void loadVideoFolders() {
        List<VideoFolder> videoFolders = getVideoFolders();
        ArrayList<String> fPath = getFolpath();
        adapter = new VideoFoldersAdapter(videoFolders, fPath);
        foldersRecyclerview.setAdapter(adapter);
    }


    private List<VideoFolder> getVideoFolders() {
        List<VideoFolder> videoFolders = new ArrayList<>();
        Set<String> uniqueFolderNames = new HashSet<>();
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.Media.DATE_MODIFIED + " DESC");
        if (cursor != null) {
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                String path = cursor.getString(pathColumn);
                String folderName = cursor.getString(folderColumn);
                String effectiveFolderName = (folderName != null && !folderName.trim().isEmpty()) ? folderName : "DefaultFolderName";
                String lowercaseFolderName = effectiveFolderName.toLowerCase();
                if (!path.startsWith("/0") && uniqueFolderNames.add(lowercaseFolderName)) {
                    if (folderName == null) {
                        folderName = "Internal Storage";
                    }
                    String folderPath = new File(path).getParent();
                    if (!FolderPath.contains(folderPath)) {
                        FolderPath.add(folderPath);
                    }
                    VideoFolder videoFolder = new VideoFolder(folderName, folderPath);
                    if (!videoFolders.contains(videoFolder)) {
                        Log.d("MainActivity", "Folder Name: " + folderName);

                        videoFolders.add(videoFolder);
                    }
                }
            }
            cursor.close();
        }

        return videoFolders;
    }



    private ArrayList<String> getFolpath() {
        ArrayList<String> fPath = new ArrayList<>();
        fPath = FolderPath;
        Log.d("FolderPath", "Folder Path: " + fPath);
        return fPath;
    }




}