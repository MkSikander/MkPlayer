package com.mbytes.mkplayer;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.LruCache;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mbytes.mkplayer.Adapter.VideoListAdapter;

import com.mbytes.mkplayer.Model.VideoItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideosListActivity extends AppCompatActivity {

    private RecyclerView videosRecyclerview;
    private VideoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_videos_list);
        videosRecyclerview = findViewById(R.id.videos_recyclerview);
        TextView videoCount = findViewById(R.id.heading);
        videosRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerview.setHasFixedSize(true);

        String folderPath = getIntent().getStringExtra("folderPath") != null ? getIntent().getStringExtra("folderPath") : "";
        String nameOFFolder = getIntent().getStringExtra("nameOfFolder") != null ? getIntent().getStringExtra("nameOfFolder") : "";
        List<VideoItem> videosInFolder = getVideosInFolder(folderPath);

        adapter = new VideoListAdapter(videosInFolder);
        videosRecyclerview.setAdapter(adapter);

        videoCount.setText( ( nameOFFolder +"(" +videosInFolder.size() +")"));

    }
    private void enableEdgeToEdge() {
        // Implement your edge-to-edge code here
    }

    private List<VideoItem> getVideosInFolder(String folderPath) {
        List<VideoItem> videosInFolder = new ArrayList<>();
        String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME};

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

            while (cursor.moveToNext()) {
                String videoName = cursor.getString(nameColumn);
                String videoPath = cursor.getString(pathColumn);
                if (videoPath.lastIndexOf(File.separator) == folderPath.length()) {

                    VideoItem videoItem = new VideoItem(videoName, videoPath);
                    videosInFolder.add(videoItem);
                }
            }

            cursor.close();
        }

        return videosInFolder;
    }


}