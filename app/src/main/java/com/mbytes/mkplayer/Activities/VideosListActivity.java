package com.mbytes.mkplayer.Activities;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.mbytes.mkplayer.Adapter.VideoListAdapter;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;
import com.mbytes.mkplayer.Utils.VideoSort;
import com.mbytes.mkplayer.Utils.VideoUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class VideosListActivity extends AppCompatActivity implements VideoListAdapter.VideoLoadListener, VideoSort.OnSortOptionSelectedListener {

    private VideoListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<VideoItem> videosList;
    private Handler mHandler;
    private Preferences sharedPreferences;
    private RecyclerView videosRecyclerview;
    private ProgressBar progressBar;
    private String nameOfFolder,folderPath;
    private TextView videoCount;
    private boolean isLoadVideoExecuted,isRefreshing;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);
        videosRecyclerview = findViewById(R.id.videos_recyclerview);
        ImageView sortImg = findViewById(R.id.img_sort);
        ImageButton backBtn = findViewById(R.id.list_back);
        mHandler = new Handler();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar=findViewById(R.id.middle_progress_bar);
        videosList = new ArrayList<>();
        videoCount = findViewById(R.id.heading);
        videosRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerview.setHasFixedSize(true);
        sharedPreferences = new Preferences(this);
        adapter = new VideoListAdapter(videosList, sharedPreferences);
        adapter.setVideoLoadListener(this);
        videosRecyclerview.setAdapter(adapter);
        VideoUtils.setAdapterCallback(adapter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            isRefreshing=true;
            mHandler.postDelayed(this::reloadVideos, 500);
        });
        if (videosList.isEmpty()) {
            isLoadVideoExecuted=true;
            loadVideos();
        }
        sortImg.setOnClickListener(view -> VideoSort.showVideoSortOptionsDialog(VideosListActivity.this, VideosListActivity.this));
        backBtn.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        if(!isLoadVideoExecuted){
            reloadVideos();
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        isLoadVideoExecuted=false;
        super.onPause();
    }
    @Override
    protected void onStop() {
        isLoadVideoExecuted=false;
        super.onStop();
    }
    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void reloadVideos() {
        if (isRefreshing) {
            videosList.clear();
            videosList.addAll(getVideosFromFolder(folderPath));
            videosList.sort(new VideoSort.VideoFilesComparator(VideosListActivity.this));
            updateNameAndCount();
            adapter.notifyDataSetChanged();
            isRefreshing=false;
            swipeRefreshLayout.setRefreshing(false);
        } else {
            if (videosList.isEmpty()) {
                loadVideos();
            }
        }
    }
    //when video is deleted or renamed by user
    @Override
    public void onVideoLoadRequested() {
        isRefreshing=true;
        reloadVideos();
    }
    private List<VideoItem> getVideosFromFolder(String folderPath) {
        List<VideoItem> videosInFolder = new ArrayList<>();
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.RESOLUTION};
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
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
            int resolutionColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION);
            while (cursor.moveToNext()) {
                String videoName = cursor.getString(nameColumn);
                String videoPath = cursor.getString(pathColumn);
                String videoDuration = cursor.getString(durationColumn);
                long dateAddedTimeStamp = cursor.getLong(dateAddedColumn);
                boolean isVideoPlayed = getVideoPlayedStatus(videoPath);
                long videoSize = cursor.getLong(sizeColumn);
                String format = cursor.getString(typeColumn);
                String videoType = format.substring(format.lastIndexOf("/") + 1);
                String videoResolution = cursor.getString(resolutionColumn);
                if (videoPath.lastIndexOf(File.separator) == folderPath.length() && videoDuration!=null) {
                    VideoItem videoItem = new VideoItem(videoName, videoPath, isVideoPlayed, videoDuration, new Date(dateAddedTimeStamp * 1000), videoSize, videoType, videoResolution);
                    videoItem.setDateAdded(new Date(dateAddedTimeStamp * 1000));
                    videosInFolder.add(videoItem);
                }
            }
            cursor.close();
        }
        return videosInFolder;
    }
    private boolean getVideoPlayedStatus(String videoPath) {
        // Retrieve video playback status from SharedPreferences
        String videoKey = "played_" + videoPath;
        return sharedPreferences.getBoolean(videoKey);
    }
    @Override
    public void onSortOptionSelected() {
        loadVideos();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void loadVideos(){
        new Thread(() -> {
            videosRecyclerview.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            folderPath = getIntent().getStringExtra("folderPath") != null ? getIntent().getStringExtra("folderPath") : "";
            List<VideoItem> result = getVideosFromFolder(folderPath);
            runOnUiThread(()->{
                videosList.clear();
                videosList.addAll(result);
                videosList.sort(new VideoSort.VideoFilesComparator(VideosListActivity.this));
                nameOfFolder = getIntent().getStringExtra("nameOfFolder") != null ? getIntent().getStringExtra("nameOfFolder") : "";
                updateNameAndCount();
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(()->{
                    progressBar.setVisibility(View.GONE);
                    videosRecyclerview.setVisibility(View.VISIBLE);
                },800);
            });
        }).start();
    }
    @SuppressLint("SetTextI18n")
    private void updateNameAndCount(){
        if (nameOfFolder != null) {
            nameOfFolder = (nameOfFolder.length() > 13) ? nameOfFolder.substring(0, 13) + "..." : nameOfFolder;
            videoCount.setText(nameOfFolder + "(" + videosList.size() + ")");
        }
    }
}