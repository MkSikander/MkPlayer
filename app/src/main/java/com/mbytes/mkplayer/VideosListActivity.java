package com.mbytes.mkplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.mbytes.mkplayer.Adapter.VideoListAdapter;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Utils.VideoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideosListActivity extends AppCompatActivity implements VideoListAdapter.VideoLoadListener {

    private RecyclerView videosRecyclerview;
    private VideoListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<VideoItem> videosList;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);
        videosRecyclerview = findViewById(R.id.videos_recyclerview);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        videosList = new ArrayList<>();
        videosRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerview.setHasFixedSize(true);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        adapter = new VideoListAdapter(videosList , sharedPreferences);
        adapter.setVideoLoadListener(this);
        videosRecyclerview.setAdapter(adapter);
        VideoUtils.setAdapterCallback(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadVideos();
            }
        });
    }
    @Override
    public void onVideoLoadRequested() {
        loadVideos();
    }

    private  List<VideoItem> getVideosInFolder(String folderPath) {
        List<VideoItem> videosInFolder = new ArrayList<>();
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME,MediaStore.Video.Media.DURATION};
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
            while (cursor.moveToNext()) {
                String videoName = cursor.getString(nameColumn);
                String videoPath = cursor.getString(pathColumn);
                String videoDuration = cursor.getString(durationColumn);
                boolean isVideoPlayed = getVideoPlayedStatus(videoPath);
                if (videoPath.lastIndexOf(File.separator) == folderPath.length()) {
                    VideoItem videoItem = new VideoItem(videoName, videoPath,isVideoPlayed,videoDuration);
                    videosInFolder.add(videoItem);
                }
            }
            cursor.close();
        }

        return videosInFolder;
    }

    public  void loadVideos(){
        String folderPath = getIntent().getStringExtra("folderPath") != null ? getIntent().getStringExtra("folderPath") : "";
        videosList.clear();
        videosList.addAll(getVideosInFolder(folderPath));
        String nameOFFolder = getIntent().getStringExtra("nameOfFolder") != null ? getIntent().getStringExtra("nameOfFolder") : "";
        TextView videoCount = findViewById(R.id.heading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoCount.setText(nameOFFolder + "(" + videosList.size() + ")");
            }
        });
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    protected void onResume() {
       loadVideos();
        super.onResume();
    }
    private boolean getVideoPlayedStatus(String videoPath) {
        // Retrieve video playback status from SharedPreferences
        String videoKey = "played_" + videoPath;
        return sharedPreferences.getBoolean(videoKey, false);
    }


}