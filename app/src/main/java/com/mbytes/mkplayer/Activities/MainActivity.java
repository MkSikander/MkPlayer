package com.mbytes.mkplayer.Activities;


import static com.mbytes.mkplayer.Utils.MainActivityHelper.isVideoFile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private LinearLayout renameLayout, deleteLayout, shareLayout, infoLayout;
    ImageView settingImg, sortImg;
    SwipeRefreshLayout refreshLayout;
    private ConstraintLayout bottomBar;
    private VideoFoldersAdapter adapter;
    private ArrayList<VideoFolder> sortedFolder;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        onCreateHelper();
    }

    void init() {
        settingImg = findViewById(R.id.img_setting);
        sortImg = findViewById(R.id.img_sort);
        infoLayout = findViewById(R.id.info_layout);
        refreshLayout = findViewById(R.id.refresh_folder);
        preferences = getSharedPreferences(MYPREF, MODE_PRIVATE);
        bottomBar = findViewById(R.id.bottomBar);
        renameLayout = findViewById(R.id.rename_layout);
        sortedFolder = new ArrayList<>();
        foldersRecyclerview = findViewById(R.id.folders_recyclerview);
        foldersRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        foldersRecyclerview.setHasFixedSize(true);
        adapter = new VideoFoldersAdapter(sortedFolder);
        foldersRecyclerview.setAdapter(adapter);
    }

    void onCreateHelper() {

        loadVideoFolders();

        settingImg.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        sortImg.setOnClickListener(view -> FolderSort.showSortOptionsDialog(MainActivity.this, MainActivity.this));

        mHandler = new Handler();
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);

            mHandler.postDelayed(mRunnable, 500);
        });

        mRunnable = () -> {
            bottomBar.setVisibility(View.GONE);
            loadVideoFolders();
        };
    }

    @Override
    public void onSortOptionSelected() {
        sortPref = preferences.getString("sort", "sortName");
        loadVideoFolders();
    }


    private void loadVideoFolders() {
        sortedFolder.clear();
        sortedFolder.addAll(getVideoFolders());
        sortedFolder.sort(new FolderSort.VideoFolderComparator(MainActivity.this));
        refreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
    }

    public List<VideoFolder> getVideoFolders() {
        List<VideoFolder> videoFolders = new ArrayList<>();
        Set<String> uniqueFolderPaths = new HashSet<>();
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATE_ADDED};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        if (cursor != null) {
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            while (cursor.moveToNext()) {
                String path = cursor.getString(pathColumn);
                String folderName = cursor.getString(folderColumn);
                long dateAddedTimeStamp = cursor.getLong(dateAddedColumn);
                String effectiveFolderName = (folderName != null && !folderName.trim().isEmpty()) ? folderName : "Internal Storage";
                String lowercaseFolderName = effectiveFolderName.toLowerCase();
                String folderPath = new File(path).getParent();
                String uniqueKey = lowercaseFolderName + folderPath;
                if (!path.startsWith("/0") && uniqueFolderPaths.add(uniqueKey)) {
                    int videoCount = noOfFiles(folderPath);
                    VideoFolder videoFolder = new VideoFolder(effectiveFolderName, folderPath, new Date(dateAddedTimeStamp * 1000),videoCount);
                    videoFolder.setDateAdded(new Date(dateAddedTimeStamp * 1000));
                    videoFolders.add(videoFolder);

                }
            }
            cursor.close();
        }
        return videoFolders;
    }

// To get Count Of Videos In Particular Folder
    private static int noOfFiles(String folderPath) {
        int fileCount = 0;
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isVideoFile(file.getPath())) {
                        fileCount++;
                    }
                }
            }
        }
        return fileCount;
    }


}