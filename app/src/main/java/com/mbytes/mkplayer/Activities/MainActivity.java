package com.mbytes.mkplayer.Activities;


import static com.mbytes.mkplayer.Utils.MainActivityHelper.convertJsonToGson;
import static com.mbytes.mkplayer.Utils.MainActivityHelper.isVideoFile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.mbytes.mkplayer.Adapter.VideoFoldersAdapter;
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.FolderSort;
import com.mbytes.mkplayer.Utils.FolderUtils;
import com.mbytes.mkplayer.Utils.Preferences;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements FolderSort.OnSortOptionSelectedListener, VideoFoldersAdapter.VideoLoadListener {


    private int position;
    private Preferences preferences;
    private ImageView settingImg, sortImg, play_last;
    private SwipeRefreshLayout refreshLayout;
    private VideoFoldersAdapter adapter;
    private ArrayList<VideoItem> videoItem;
    private ArrayList<VideoFolder> sortedFolder;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        onCreateHelper();
    }
    void init() {
        settingImg = findViewById(R.id.img_setting);
        sortImg = findViewById(R.id.img_sort);
        refreshLayout = findViewById(R.id.refresh_folder);
        preferences = new Preferences(this);
        preferences.setStoragePermission("OK");
        play_last = findViewById(R.id.play_last_playing);
        sortedFolder = new ArrayList<>();
        RecyclerView foldersRecyclerview = findViewById(R.id.folders_recyclerview);
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
        sortImg.setOnClickListener(view -> FolderSort.showQuickSettingDialog(MainActivity.this, MainActivity.this));
        mHandler = new Handler();
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            mHandler.postDelayed(this::loadVideoFolders, 500);
        });
    }
    @Override
    public void onSortOptionSelected() {
        refreshLayout.setRefreshing(true);
        mHandler.postDelayed(this::loadVideoFolders,1000);

    }
    @SuppressLint("NotifyDataSetChanged")
    private void loadVideoFolders() {
        sortedFolder.clear();
        sortedFolder.addAll(getVideoFolders());
        sortedFolder.sort(new FolderSort.VideoFolderComparator(MainActivity.this));
        refreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
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
    private int newVideosCount(String folderPath){
        int count=0;
        List<VideoItem> videosInFolder=new ArrayList<>();
        String[] projection = {MediaStore.Video.Media.DATA,MediaStore.Video.Media.DURATION};
        String selection = MediaStore.Video.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{folderPath + "/%"};
        Cursor cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        if (cursor != null) {
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            while (cursor.moveToNext()) {
                String videoPath = cursor.getString(pathColumn);
                String videoDuration = cursor.getString(durationColumn);
                if (videoPath.lastIndexOf(File.separator) == folderPath.length() && videoDuration != null) {
                    VideoItem videoItem = new VideoItem(videoPath, videoDuration,"");
                    videosInFolder.add(videoItem);
                }
            }
            cursor.close();
        }
        for(int i=0;i<videosInFolder.size();i++){
            String videoPath=videosInFolder.get(i).getVideoPath();
            String videoKey = "played_" + videoPath;
            boolean playedStatus=preferences.getBoolean(videoKey);
            if (!playedStatus){
                count++;
            }
        }
        return count;
    }
    @Override
    public void onVideoLoadRequested() {
        mHandler.postDelayed(this::loadVideoFolders,200);
    }
    @SuppressLint("NotifyDataSetChanged")
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onResume() {
        if (preferences.isUpdateFolder()){
            preferences.updateFolders(false);
            reloadFolders();
        }
        getLastVideos();
        if (!(videoItem == null)) {
            play_last.setVisibility(View.VISIBLE);
            play_last.setOnClickListener(view -> {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra("position", position);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("videoArrayList", videoItem);
                intent.putExtras(bundle);
                startActivity(intent);
            });
        }
        super.onResume();

    }

    private void reloadFolders() {
        mHandler.postDelayed(this::loadVideoFolders,200);
    }

    public List<VideoFolder> getVideoFolders() {
        List<VideoFolder> videoFolders = new ArrayList<>();
        Set<String> uniqueFolderPaths = new HashSet<>();
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.SIZE};
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
                    int newVideos=newVideosCount(folderPath);
                    long folderSize = calculateFolderSize(folderPath);
                    Log.d("Folder Size ", "folder Size in Cursor" + folderSize);
                    VideoFolder videoFolder = new VideoFolder(effectiveFolderName, folderPath, new Date(dateAddedTimeStamp * 1000), videoCount, folderSize,newVideos);
                    videoFolder.setDateAdded(new Date(dateAddedTimeStamp * 1000));
                    videoFolders.add(videoFolder);
                }
            }
            cursor.close();
        }
        return videoFolders;
    }

    private void getLastVideos() {
        String json = preferences.getString("lastVideos");
        position = preferences.getVideoPosition("lastPosition");
        videoItem = convertJsonToGson(json);
    }
}