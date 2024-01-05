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
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.mbytes.mkplayer.R;

public class PlayerActivity extends AppCompatActivity {


    private SimpleExoPlayer player;
    private PlayerView playerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.player_view);
        player = new SimpleExoPlayer.Builder(PlayerActivity.this).build();
        String videoPath = getIntent().getStringExtra("path");

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(PlayerActivity.this, "Mk Player")
        ).createMediaSource(MediaItem.fromUri(Uri.parse(videoPath)));

        int videoRotation = getVideoRotation(videoPath);
        setRequestedOrientation(videoRotation);

        playerView.setPlayer(player);
        player.prepare(mediaSource);

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
        player.release();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Pause the player when the activity is not visible
        if (player != null) {
            player.setPlayWhenReady(false);
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