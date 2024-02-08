package com.mbytes.mkplayer.Player;

import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.convertVideoListToJson;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.hideBottomSheet;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.setAudioTrack;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.setPlaybackSpeed;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.setSubTrack;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.showPlaylistVideos;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.ui.PlayerView;

import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.Utils.BrightnessManager;
import com.mbytes.mkplayer.Player.Utils.PlayerGestureHelper;
import com.mbytes.mkplayer.Player.Utils.VolumeManager;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Player.Utils.PlayerUtils;
import com.mbytes.mkplayer.Utils.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    public PlayerView playerView;
    private ControlsMode controlsMode;
    public static boolean isControlLocked = false;
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
    private ArrayList<VideoItem> playerVideos = new ArrayList<>();
    private TextView title, volume_text, brightness_text, seek_duration, seek_change, startOver;
    private DefaultTrackSelector trackSelector;
    private Preferences preferences;
    private int position;
    private int newPosition;
    private ImageView nextBtn, prevBtn, backBtn, scalingBtn, lockBtn, unlockBtn, audioTrack, subTitleTrack, cancelStartOver,playbackSpeed,playList;
    //Gesture Related import
    private ProgressBar progressBar, volProgress, briProgress;
    private LinearLayout bri_layout, vol_layout, startOverLayout;
    private AudioManager audioManager;
    private RelativeLayout root;
    private BrightnessManager brightnessManager;
    private VolumeManager volumeManager;
    private PlayerGestureHelper playerGestureHelper;
    private FrameLayout zoomLayout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        initViews(savedInstanceState);
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

    private void initViews(Bundle savedInstanceState) {
        preferences = new Preferences(PlayerActivity.this);
        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.exo_mid_progress);
        trackSelector = new DefaultTrackSelector(this);
        nextBtn = findViewById(R.id.exo_next_btn);
        prevBtn = findViewById(R.id.exo_prev);
        zoomLayout=findViewById(R.id.zoom_layout);
        backBtn = findViewById(R.id.video_back);
        title = findViewById(R.id.video_title);
        scalingBtn = findViewById(R.id.scaling);
        lockBtn = findViewById(R.id.lock);
        unlockBtn = findViewById(R.id.unlock);
        startOver = findViewById(R.id.start_over);
        cancelStartOver = findViewById(R.id.start_over_cancel);
        startOverLayout = findViewById(R.id.start_over_layout);
        root = findViewById(R.id.root_layout);
        audioTrack = findViewById(R.id.audio_track);
        subTitleTrack = findViewById(R.id.exo_subtitle_track);
        playbackSpeed=findViewById(R.id.playback_speed);
        playList=findViewById(R.id.playlist);
        bri_layout = findViewById(R.id.brightness_gesture_layout);
        vol_layout = findViewById(R.id.volume_gesture_layout);
        volProgress = findViewById(R.id.volume_progress_bar);
        seek_duration = findViewById(R.id.seek_duration);
        seek_change = findViewById(R.id.seek_change);
        briProgress = findViewById(R.id.brightness_progress_bar);
        volume_text = findViewById(R.id.volume_progress_text);
        brightness_text = findViewById(R.id.brightness_progress_text);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        volumeManager = new VolumeManager(audioManager);
        brightnessManager = new BrightnessManager(this);
        newPosition=getIntent().getIntExtra("VIDEO_POSITION",-1);
        if (newPosition>-1){
            playThis(newPosition);
        }
        playerGestureHelper = new PlayerGestureHelper(this, audioManager, brightnessManager, volumeManager);
        position = getIntent().getIntExtra("position", 1);
        playerVideos = Objects.requireNonNull(getIntent().getExtras()).getParcelableArrayList("videoArrayList");
        nextBtn.setOnClickListener(view -> PlayNext());
        prevBtn.setOnClickListener(view -> PlayPrev());
        backBtn.setOnClickListener(view -> finish());
        lockBtn.setOnClickListener(view -> {
            controlsMode = ControlsMode.LOCK;
            root.setVisibility(View.INVISIBLE);
            unlockBtn.setVisibility(View.VISIBLE);
            isControlLocked = true;
        });
        unlockBtn.setOnClickListener(view -> {
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            unlockBtn.setVisibility(View.INVISIBLE);
            isControlLocked = false;
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
                zoomLayout.setScaleX(1f);
                zoomLayout.setScaleY(1f);
               playerGestureHelper.resetScaleFactor();
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
        startOver.setOnClickListener(view -> {
            player.seekTo(0L);
            startOverLayout.setVisibility(View.GONE);
        });
        cancelStartOver.setOnClickListener(view -> {
            startOverLayout.setVisibility(View.VISIBLE);
        });
    }
    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        Long skipPosition = preferences.getLong(playerVideos.get(position).getVideoPath());
        setRequestedOrientation(PlayerUtils.getVideoRotation(playerVideos.get(position).getVideoPath()));
        String path = playerVideos.get(position).getVideoPath();
        title.setText(playerVideos.get(position).getVideoName());
        DefaultExtractorsFactory extractorsFactory =
                new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
        if (player == null) {
            player = new ExoPlayer.Builder(this).setTrackSelector(trackSelector)
                    .setMediaSourceFactory(new DefaultMediaSourceFactory(this,extractorsFactory))
                    .setSeekParameters(SeekParameters.CLOSEST_SYNC)
                    .build();
            playerView.setPlayer(player);
        }
        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(path)));
        player.setMediaItem(mediaItem);

        boolean haveStartPosition = startItemIndex != C.INDEX_UNSET;
        //check for skip position
        if (haveStartPosition) {
            showStartOverLayout();
            player.seekTo(startItemIndex, startPosition);
        } else if (skipPosition > 0L) {
            showStartOverLayout();
            player.seekTo(skipPosition);
        } else {
            player.seekTo(skipPosition);
        }
        playerView.setKeepScreenOn(true);

        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState != Player.STATE_READY) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        player.prepare();


        if (!player.isPlaying())
        {
                playerView.setKeepScreenOn(false);
        }
        player.play();


        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == Player.STATE_ENDED) {
                    String key = playerVideos.get(position).getVideoPath();
                    Long currentPosition = 0L;
                    preferences.setLong(key, currentPosition);

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
            setSubTrack(player, trackSelector, PlayerActivity.this);
        });
        playbackSpeed.setOnClickListener(view -> {
            player.pause();
            setPlaybackSpeed(player,PlayerActivity.this);
        });
        playList.setOnClickListener(view -> {
            showPlaylistVideos(playerVideos,position,PlayerActivity.this);
        });

        String videoPath = playerVideos.get(position).getVideoPath();
        if (!getVideoPlayedStatus(videoPath)) {
            setVideoPlayedStatus(videoPath);
        }


    }

    private void showStartOverLayout() {

        Handler startOverShow = new Handler();
        Handler startOverHide = new Handler();
        //show StartOverLayout
        startOverShow.postDelayed(new Runnable() {
            @Override
            public void run() {
                startOverLayout.setVisibility(View.VISIBLE);
            }
        }, 1000);
//hide startOverLayout
        startOverHide.postDelayed(new Runnable() {
            @Override
            public void run() {
                startOverLayout.setVisibility(View.GONE);
            }
        }, 6000);

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

    public void playThis(int newPosition){
        try {
            player.stop();
            position=newPosition;
            hideBottomSheet();
            initializePlayer();
        }
        catch (Exception ignored){

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
        // Get video playback status from SharedPreferences
        // Use a unique key for each video
        String videoKey = "played_" + videoPath;
        return preferences.getBoolean(videoKey);

    }

    public void hideProgressBar() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your existing code here
                if (vol_layout.getVisibility() == View.VISIBLE) {
                    vol_layout.setVisibility(View.GONE);
                }
                if (bri_layout.getVisibility() == View.VISIBLE) {
                    bri_layout.setVisibility(View.GONE);
                }
                if (seek_change.getVisibility() == View.VISIBLE) {
                    seek_change.setVisibility(View.GONE);
                    seek_duration.setVisibility(View.GONE);
                }
            }
        }, 800);
    }

    public float getCurrentBrightness() {
        return getWindow().getAttributes().screenBrightness;

    }

    public boolean isControlLocked() {
        return isControlLocked;
    }

    public int getVolLayoutVisibility() {
        return vol_layout.getVisibility();
    }

    public int getBriLayoutVisibility() {
        return bri_layout.getVisibility();
    }

    public void setSeekTextVisible() {
        seek_change.setVisibility(View.VISIBLE);
        seek_duration.setVisibility(View.VISIBLE);
    }

    public void setBriVisible() {
        bri_layout.setVisibility(View.VISIBLE);
    }

    public void setVolVisible() {
        vol_layout.setVisibility(View.VISIBLE);
    }

    public void showBriGestureLayout() {
        brightness_text.setText(String.valueOf(brightnessManager.getBrightnessPercentage()));
        briProgress.setMax((int) (brightnessManager.maxBrightness * 16));
        briProgress.setProgress((int) (brightnessManager.getCurrentBrightness() * 16));
    }

    public void showVolGestureLayout() {
        volume_text.setText(String.valueOf(volumeManager.getVolumePercentage()));
        volProgress.setMax((int) (volumeManager.getMaxVolume() * 16));
        volProgress.setProgress((int) (volumeManager.getCurrentVolume() * 16));
    }

    public void showSeekInfo(String s, String s1) {
        seek_duration.setText(s);
        seek_change.setText(s1);
    }

    public void setStartOverVisibility() {
        startOverLayout.setVisibility(View.GONE);
    }

}