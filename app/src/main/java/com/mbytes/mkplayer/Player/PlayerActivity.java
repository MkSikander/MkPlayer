package com.mbytes.mkplayer.Player;


import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.mbytes.mkplayer.R;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {


    private ExoPlayer player;
    private PlayerView playerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.player_view);

        String videoPath = getIntent().getStringExtra("path");

        setRequestedOrientation(getVideoRotation(videoPath));

        initializePlayer(videoPath);

    }

    private void initializePlayer(String videoPath) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(videoPath)));
// Set the media item to be played.
        player.setMediaItem(mediaItem);
// Prepare the player.
        player.prepare();
// Start the playback.
        player.play();
    }

    //getting video Orientation
    private int getVideoRotation(String videoPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            // Swap width and height for portrait videos
            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
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
        if(player!= null)
        {
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
    public void onBackPressed() {
        super.onBackPressed();

        // Release the player when the back button is pressed
        if (player != null) {
            player.setPlayWhenReady(false);
            player.release();
            player = null;
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();

        // Resume playback when the activity is restarted
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Restore the saved video position

        // Start playback
        player.setPlayWhenReady(true);
    }



}