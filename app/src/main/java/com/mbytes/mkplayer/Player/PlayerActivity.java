package com.mbytes.mkplayer.Player;


import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ConcatenatingMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {


    private ExoPlayer player;
    private PlayerView playerView;
    String videoPath, videoTitle;
    ArrayList<VideoItem> playerVideos = new ArrayList<>();
    TextView title;
    int position;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextBtn, prevBtn,backBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.player_view);
        nextBtn = findViewById(R.id.exo_next_btn);
        prevBtn = findViewById(R.id.exo_prev);
        backBtn =findViewById(R.id.video_back);
        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        playerVideos = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        title = findViewById(R.id.video_title);
        nextBtn.setOnClickListener(view -> PlayNext());
        prevBtn.setOnClickListener(view -> PlayPrev());
        backBtn.setOnClickListener(view -> finish());

        initializePlayer();


    }

    @OptIn(markerClass = UnstableApi.class)


    private void initializePlayer() {
        try {
            player.release();
        } catch (Exception e) {
        }

        setRequestedOrientation(getVideoRotation(playerVideos.get(position).getVideoPath()));
        String path = playerVideos.get(position).getVideoPath();
        title.setText(playerVideos.get(position).getVideoName());
        player = new ExoPlayer.Builder(this).build();

        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(path)));
        player.setMediaItem(mediaItem);
        playerView.setPlayer(player);
        // Build the media item.
        playerView.setKeepScreenOn(true);
        player.seekTo(position, C.TIME_UNSET);
        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        player.prepare();
        player.play();


        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                Log.d("Player Activity", "State Ended" + playbackState);
                if (playbackState == Player.STATE_ENDED) {
                    Log.d("Player Activity", "State Ended" + playbackState);
                    if (position == playerVideos.size() - 1) {
                        position = 0;
                        finish();
                    } else {
                        PlayNext();
                    }
                }
            }
        });
    }

    //getting video Orientation
    private int getVideoRotation(String videoPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            // Swap width and height for portrait videos
            int width = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
            int height = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            if (width > height) {
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }


    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Pause the player when the activity is not visible
        if (player != null) {
            player.setPlayWhenReady(false);

            player.release();
            // Release the player here
            player = null; // Set player to null to indicate it's released
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
        player.getPlaybackState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Restore the saved video position

        // Start playback
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void PlayNext() {
        try {
            player.stop();
            position++;
            if (position < playerVideos.size()) {
                initializePlayer();
            } else {
                position = 0;
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "No More Videos", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void PlayPrev() {
        try {
            player.stop();
            position--;
            initializePlayer();
        } catch (Exception e) {
            Toast.makeText(this, "No More Videos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}