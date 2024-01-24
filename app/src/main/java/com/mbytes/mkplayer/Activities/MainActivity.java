package com.mbytes.mkplayer.Activities;


import static com.mbytes.mkplayer.Utils.MainActivityHelper.isVideoFile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.mbytes.mkplayer.Adapter.VideoFoldersAdapter;
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.FolderSort;
import com.mbytes.mkplayer.Utils.FolderUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements FolderSort.OnSortOptionSelectedListener, VideoFoldersAdapter.VideoLoadListener {

    private static final String MYPREF = "mypref";
    private RecyclerView foldersRecyclerview;
    private long tempSize;
    private String sortPref = "sortName",path,videoTitle,json;
    private int position;
    private SharedPreferences preferences;
    ArrayList<VideoItem> playerVideos = new ArrayList<>();
    private LinearLayout renameLayout, deleteLayout, shareLayout, infoLayout;
    ImageView settingImg, sortImg, play_last;
    SwipeRefreshLayout refreshLayout;

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
        play_last = findViewById(R.id.play_last_playing);
        renameLayout = findViewById(R.id.rename_layout);
        sortedFolder = new ArrayList<>();
        foldersRecyclerview = findViewById(R.id.folders_recyclerview);
        foldersRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        foldersRecyclerview.setHasFixedSize(true);
        adapter = new VideoFoldersAdapter(sortedFolder);
        adapter.setVideoLoadListener(this);
        foldersRecyclerview.setAdapter(adapter);
        FolderUtils.setAdapterCallback(adapter);
    }

    @OptIn(markerClass = UnstableApi.class)
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

        mRunnable = this::loadVideoFolders;
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
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.SIZE};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        if (cursor != null) {
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int videoSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            while (cursor.moveToNext()) {
                String path = cursor.getString(pathColumn);
                String folderName = cursor.getString(folderColumn);
                long dateAddedTimeStamp = cursor.getLong(dateAddedColumn);
                long videoSize = cursor.getLong(videoSizeColumn);
                String effectiveFolderName = (folderName != null && !folderName.trim().isEmpty()) ? folderName : "Internal Storage";
                String lowercaseFolderName = effectiveFolderName.toLowerCase();
                String folderPath = new File(path).getParent();
                String uniqueKey = lowercaseFolderName + folderPath;
                if (!path.startsWith("/0") && uniqueFolderPaths.add(uniqueKey)) {
                    int videoCount = noOfFiles(folderPath);
                    long folderSize = calculateFolderSize(folderPath);
                    Log.d("Folder Size ", "folder Size in Cursor" + folderSize);
                    VideoFolder videoFolder = new VideoFolder(effectiveFolderName, folderPath, new Date(dateAddedTimeStamp * 1000), videoCount, folderSize);
                    videoFolder.setDateAdded(new Date(dateAddedTimeStamp * 1000));
                    videoFolders.add(videoFolder);
                }
            }
            cursor.close();
        }
        return videoFolders;
    }

    private long calculateFolderSize(String folderPath) {
        long totalSize = 0;
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isVideoFile(file.getPath())) {
                        totalSize += file.length();
                    }
                }
            }
        }
        return totalSize;
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


    @Override
    public void onVideoLoadRequested() {
        loadVideoFolders();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}