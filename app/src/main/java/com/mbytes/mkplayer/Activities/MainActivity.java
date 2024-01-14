package com.mbytes.mkplayer.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mbytes.mkplayer.Adapter.VideoFoldersAdapter;
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.FolderSort;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements FolderSort.OnSortOptionSelectedListener {

    private static final String MYPREF = "mypref";
    private RecyclerView foldersRecyclerview;
    private String sortPref = "sortName";
    private SharedPreferences preferences;
    ArrayList<String> FolderPath;
    ImageView settingImg,sortImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settingImg = findViewById(R.id.img_setting);
        sortImg = findViewById(R.id.img_sort);
        preferences = getSharedPreferences(MYPREF, MODE_PRIVATE);
        FolderPath = new ArrayList<>();
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
        settingImg.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        sortImg.setOnClickListener(view -> FolderSort.showSortOptionsDialog(MainActivity.this, MainActivity.this));
    }
    @Override
    public void onSortOptionSelected() {
        sortPref = preferences.getString("sort", "sortName");
        Log.d("SortPreference", "Selected sort preference: " + sortPref);
        loadVideoFolders();
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
        videoFolders.sort(new FolderSort.VideoFolderComparator(MainActivity.this));
        Log.d("SortPreference", "Selected sort preference: " + sortPref);
        // Create a sorted copy of the FolderPath list based on the sorted videoFolders list
        ArrayList<String> sortedFPath = new ArrayList<>();
        for (VideoFolder folder : videoFolders) {
            String folderPath = folder.getFolderPath();
            if (fPath.contains(folderPath)) {
                sortedFPath.add(folderPath);
            }
        }
        VideoFoldersAdapter adapter = new VideoFoldersAdapter(videoFolders, sortedFPath);
        foldersRecyclerview.setAdapter(adapter);
    }


    private List<VideoFolder> getVideoFolders() {
        List<VideoFolder> videoFolders = new ArrayList<>();
        Set<String> uniqueFolderPaths = new HashSet<>();
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media.DATE_ADDED};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,null);
        if (cursor != null) {
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int dateAddedColumn=cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            while (cursor.moveToNext()) {
                String path = cursor.getString(pathColumn);
                String folderName = cursor.getString(folderColumn);
                long dateAddedTimeStamp = cursor.getLong(dateAddedColumn);
                String effectiveFolderName = (folderName != null && !folderName.trim().isEmpty()) ? folderName : "Internal Storage";
                String lowercaseFolderName = effectiveFolderName.toLowerCase();
                String folderPath = new File(path).getParent();
                String uniqueKey = lowercaseFolderName + folderPath;
                if (!path.startsWith("/0") && uniqueFolderPaths.add(uniqueKey)) {

                    if (!FolderPath.contains(folderPath)) {
                        FolderPath.add(folderPath);
                    }
                    VideoFolder videoFolder = new VideoFolder(effectiveFolderName, folderPath, new Date(dateAddedTimeStamp * 1000));

                    videoFolder.setDateAdded(new Date(dateAddedTimeStamp*1000));

                       // Log.d("MainActivity", "Folder Name: " + folderName);

                        videoFolders.add(videoFolder);

                }
            }
            cursor.close();
        }

        return videoFolders;
    }


    private ArrayList<String> getFolpath() {
        ArrayList<String> fPath;
        fPath = FolderPath;
        Log.d("FolderPath", "Folder Path: " + fPath);
        return fPath;
    }

}