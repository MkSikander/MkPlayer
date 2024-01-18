package com.mbytes.mkplayer.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.mbytes.mkplayer.Adapter.VideoListAdapter;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.VideoSort;
import com.mbytes.mkplayer.Utils.VideoUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideosListActivity extends AppCompatActivity implements VideoListAdapter.VideoLoadListener, VideoSort.OnSortOptionSelectedListener {

    private VideoListAdapter adapter;
    private static final String MYPREF = "mypref";
    private SharedPreferences preferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<VideoItem> videosList;
    private Handler mHandler;
    private Runnable mRunnable;
    private SharedPreferences sharedPreferences;
    ImageView sortImg;
    ImageButton backBtn;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);
        RecyclerView videosRecyclerview = findViewById(R.id.videos_recyclerview);
        preferences = getSharedPreferences(MYPREF, MODE_PRIVATE);
        sortImg = findViewById(R.id.img_sort);
        backBtn = findViewById(R.id.list_back);
        mHandler = new Handler();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        videosList = new ArrayList<>();
        videosRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerview.setHasFixedSize(true);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        adapter = new VideoListAdapter(videosList, sharedPreferences);
        adapter.setVideoLoadListener(this);
        videosRecyclerview.setAdapter(adapter);
        VideoUtils.setAdapterCallback(adapter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            mHandler.postDelayed(mRunnable, 500);
        });

        mRunnable = this::loadVideos;


        sortImg.setOnClickListener(view -> VideoSort.showVideoSortOptionsDialog(VideosListActivity.this, VideosListActivity.this));
        backBtn.setOnClickListener(view -> finish());
    }

    @Override
    public void onVideoLoadRequested() {
        loadVideos();
    }

    private List<VideoItem> getVideosInFolder(String folderPath) {
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
            int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION);
            int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION);
            while (cursor.moveToNext()) {
                String videoName = cursor.getString(nameColumn);
                String videoPath = cursor.getString(pathColumn);
                String videoDuration = cursor.getString(durationColumn);
                long dateAddedTimeStamp = cursor.getLong(dateAddedColumn);
                boolean isVideoPlayed = getVideoPlayedStatus(videoPath);
                long videoSize = cursor.getLong(sizeColumn);
                String format = cursor.getString(typeColumn);
                String videoType = format.substring(format.lastIndexOf("/") + 1);
                String videoWith = cursor.getString(widthColumn);
                String videoHeight = cursor.getString(heightColumn);
                String videoResolution = (videoWith + " x " + videoHeight);
                if (videoPath.lastIndexOf(File.separator) == folderPath.length()) {
                    VideoItem videoItem = new VideoItem(videoName, videoPath, isVideoPlayed, videoDuration, new Date(dateAddedTimeStamp * 1000), videoSize, videoType, videoResolution);
                    videoItem.setDateAdded(new Date(dateAddedTimeStamp * 1000));
                    videosInFolder.add(videoItem);
                }
            }
            cursor.close();
        }
        return videosInFolder;
    }

    @SuppressLint({"NotifyDataSetChanged"})
    public void loadVideos() {
        new LoadVideosTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadVideosTask extends AsyncTask<Void, Void, List<VideoItem>> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected List<VideoItem> doInBackground(Void... params) {
            String folderPath = getIntent().getStringExtra("folderPath") != null ? getIntent().getStringExtra("folderPath") : "";
            return getVideosInFolder(folderPath);
        }

        @Override
        protected void onPostExecute(List<VideoItem> result) {
            videosList.clear();
            videosList.addAll(result);
            videosList.sort(new VideoSort.VideoFilesComparator(VideosListActivity.this));
            String nameOfFolder = getIntent().getStringExtra("nameOfFolder") != null ? getIntent().getStringExtra("nameOfFolder") : "";
            TextView videoCount = findViewById(R.id.heading);
            assert nameOfFolder != null;
            nameOfFolder = (nameOfFolder.length() > 13) ? nameOfFolder.substring(0, 13) + "..." : nameOfFolder;
            videoCount.setText(nameOfFolder + "(" + videosList.size() + ")");
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
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

    @Override
    public void onSortOptionSelected() {
        String sortPref = preferences.getString("sortVideo", "sortName");
        Log.d("SortPreference", "Selected sort preference: " + sortPref);
        loadVideos();
    }

}