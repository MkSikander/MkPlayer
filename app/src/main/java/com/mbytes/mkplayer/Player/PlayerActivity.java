package com.mbytes.mkplayer.Player;

import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
import static com.google.gson.internal.$Gson$Types.arrayOf;
import static com.mbytes.mkplayer.Utils.PlayerUtils.convertVideoListToJson;
import static com.mbytes.mkplayer.Utils.PlayerUtils.setAudioTrack;
import static com.mbytes.mkplayer.Utils.PlayerUtils.setSubTrack;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelection;
import androidx.media3.extractor.mp4.Track;
import androidx.media3.ui.PlayerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.PlayerUtils;
import com.mbytes.mkplayer.Utils.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {


    private ExoPlayer player;
    private PlayerView playerView;
    private ControlsMode controlsMode;
    private boolean startAutoPlay;
    private int startItemIndex;
    private long startPosition;
    private TrackSelectionParameters trackSelectionParameters;

    public enum ControlsMode {
        LOCK, FULLSCREEN
    }

    private static final String KEY_TRACK_SELECTION_PARAMETERS = "track_selection_parameters";
    private static final String KEY_ITEM_INDEX = "item_index";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private String path;
    private ArrayList<VideoItem> playerVideos = new ArrayList<>();
    private TextView title;
    private DefaultTrackSelector trackSelector;
    private Preferences preferences;
    private ProgressBar progressBar;

    int position;
    private RelativeLayout root;
    private ImageView nextBtn, prevBtn, backBtn, scalingBtn, lockBtn, unlockBtn, audioTrack, subTitleTrack;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        initViews();
        position = getIntent().getIntExtra("position", 1);
        playerVideos = Objects.requireNonNull(getIntent().getExtras()).getParcelableArrayList("videoArrayList");
        nextBtn.setOnClickListener(view -> PlayNext());
        prevBtn.setOnClickListener(view -> PlayPrev());
        backBtn.setOnClickListener(view -> finish());
        lockBtn.setOnClickListener(view -> {
            controlsMode = ControlsMode.LOCK;
            root.setVisibility(View.INVISIBLE);
            unlockBtn.setVisibility(View.VISIBLE);

        });
        unlockBtn.setOnClickListener(view -> {
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            unlockBtn.setVisibility(View.INVISIBLE);
        });
        if (savedInstanceState != null) {
            trackSelectionParameters =
                    TrackSelectionParameters.fromBundle(
                            Objects.requireNonNull(savedInstanceState.getBundle(KEY_TRACK_SELECTION_PARAMETERS)));
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            position = savedInstanceState.getInt(KEY_ITEM_INDEX);
            startPosition = savedInstanceState.getLong(KEY_POSITION);

        } else {
            trackSelectionParameters = new TrackSelectionParameters.Builder(/* context= */ this).build();
            clearStartPosition();
        }
        scalingBtn.setOnClickListener(v -> {
            int currentMode = playerView.getResizeMode();
            int newMode = RESIZE_MODE_FIT; // Default to fit
            switch (currentMode) {
                case RESIZE_MODE_FIT:
                    newMode = RESIZE_MODE_FILL;
                    break;
                case RESIZE_MODE_FILL:
                    newMode = RESIZE_MODE_ZOOM;
                    break;
                case RESIZE_MODE_ZOOM:
                    newMode = RESIZE_MODE_FIXED_HEIGHT;
                    break;
            }
            playerView.setResizeMode(newMode);
        });


        initializePlayer();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putBundle(KEY_TRACK_SELECTION_PARAMETERS, trackSelectionParameters.toBundle());
        outState.putInt(KEY_ITEM_INDEX, startItemIndex);
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putLong(KEY_POSITION, startPosition);
    }

    private void initViews() {
        preferences = new Preferences(PlayerActivity.this);
        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.exo_mid_progress);
        trackSelector = new DefaultTrackSelector(this);
        nextBtn = findViewById(R.id.exo_next_btn);
        prevBtn = findViewById(R.id.exo_prev);
        backBtn = findViewById(R.id.video_back);
        title = findViewById(R.id.video_title);
        scalingBtn = findViewById(R.id.scaling);
        lockBtn = findViewById(R.id.lock);
        unlockBtn = findViewById(R.id.unlock);
        root = findViewById(R.id.root_layout);
        audioTrack = findViewById(R.id.audio_track);
        subTitleTrack=findViewById(R.id.exo_subtitle_track);
    }
    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {

        Long skipPosition = preferences.getLong(playerVideos.get(position).getVideoPath());
        setRequestedOrientation(PlayerUtils.getVideoRotation(playerVideos.get(position).getVideoPath()));
        path = playerVideos.get(position).getVideoPath();
        title.setText(playerVideos.get(position).getVideoName());
        if (player == null) {
            player = new ExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
            playerView.setPlayer(player);
        }
        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(path)));
        player.setMediaItem(mediaItem);
        playerView.setKeepScreenOn(true);
        boolean haveStartPosition = startItemIndex != C.INDEX_UNSET;
        //check for skip position
        if (haveStartPosition) {
            player.seekTo(startItemIndex, startPosition);
        } else player.seekTo(skipPosition);
        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState != Player.STATE_READY) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == Player.STATE_ENDED) {
                    if (position == playerVideos.size() - 1) {
                        position = 0;
                        finish();
                    } else {
                        PlayNext();
                    }
                }
            }

        });
        audioTrack.setOnClickListener(v -> {
            player.pause();
            setAudioTrack(player, trackSelector, PlayerActivity.this);
        });
        subTitleTrack.setOnClickListener(view -> {
            player.pause();
            setSubTrack(player,trackSelector,PlayerActivity.this);
        });
        String videoPath= playerVideos.get(position).getVideoPath();
        if (!getVideoPlayedStatus(videoPath)) {
            setVideoPlayedStatus(videoPath);
        }

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

    protected void releasePlayer() {
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            String key = playerVideos.get(position).getVideoPath();
            Long currentPosition = Math.max(0, player.getContentPosition());
            preferences.setLong(key, currentPosition);
            player.release();
            player = null;
            playerView.setPlayer(/* player= */ null);
        }
    }

    //for bundle instances To resume video where it was left (during Calls and Interrupt)
    private void updateTrackSelectorParameters() {
        if (player != null) {
            trackSelectionParameters = player.getTrackSelectionParameters();
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startItemIndex = player.getCurrentMediaItemIndex();
            startPosition = Math.max(0, player.getContentPosition());

        }
    }

    protected void clearStartPosition() {
        startAutoPlay = true;
        startItemIndex = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (playerView != null) {
            playerView.onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player == null) {
            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onPause() {

        if (player != null) {
            player.pause();
        }
        super.onPause();


    }

    @Override
    public void onStop() {
        if (player != null) {
            player.stop();
        }
        setLastVideos();
        super.onStop();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();

    }

    private void setLastVideos() {
        String videoListJson = convertVideoListToJson(playerVideos);
        preferences.setLastVideos("lastVideos", "lastPosition", position, videoListJson);
    }

    private void setVideoPlayedStatus(String videoPath) {
        // Save video playback status to SharedPreferences
        // Use a unique key for each video
        String videoKey = "played_" + videoPath;
        preferences.setBoolean(videoKey, true);

    }
    private boolean getVideoPlayedStatus(String videoPath) {
        // Save video playback status to SharedPreferences
        // Use a unique key for each video
        String videoKey = "played_" + videoPath;
       return preferences.getBoolean(videoKey);

    }

}