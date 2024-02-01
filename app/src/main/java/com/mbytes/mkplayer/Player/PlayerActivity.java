package com.mbytes.mkplayer.Player;

import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
import static com.mbytes.mkplayer.Utils.PlayerUtils.convertVideoListToJson;
import static com.mbytes.mkplayer.Utils.PlayerUtils.setAudioTrack;
import static com.mbytes.mkplayer.Utils.PlayerUtils.setSubTrack;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerView;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.PlayerUtils;
import com.mbytes.mkplayer.Utils.Preferences;
import com.mbytes.mkplayer.Utils.VideoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@UnstableApi
public class PlayerActivity extends AppCompatActivity  {


    public static ExoPlayer player;
    private PlayerView playerView;
    private ControlsMode controlsMode;
    private boolean startAutoPlay;

    public  static boolean isControlLocked=false;
    private int startItemIndex;
    private final float SCROLL_STEP = PlayerUtils.dpToPx(16);
    private int brightness=0;
    private long startPosition;
    private TrackSelectionParameters trackSelectionParameters;
    private int volume;

    public enum ControlsMode {
        LOCK, FULLSCREEN
    }

    private static final String KEY_TRACK_SELECTION_PARAMETERS = "track_selection_parameters";
    private static final String KEY_ITEM_INDEX = "item_index";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private String path;
    private float gestureScrollY = 0f;
    private ArrayList<VideoItem> playerVideos = new ArrayList<>();
    private TextView title,volume_text,brightness_text,seek_text;
    private DefaultTrackSelector trackSelector;
    private Preferences preferences;
    private ProgressBar progressBar,volProgress,briProgress;
    private LinearLayout vol_layout,bri_layout;
    private AudioManager audioManager;
    private int position;
    private RelativeLayout root,zoomLayout,layout_main;

    private GestureDetector gestureDetector;

    private final float IGNORE_BORDER = PlayerUtils.dpToPx(24);
    private ImageView nextBtn, prevBtn, backBtn, scalingBtn, lockBtn, unlockBtn, audioTrack, subTitleTrack;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        initViews(savedInstanceState);
        initializePlayer();
        playerView.setOnTouchListener((view, motionEvent) -> {
        int Action= motionEvent.getAction();
        if (Action==MotionEvent.ACTION_UP){
            hideProgressBar();
        }
           gestureDetector.onTouchEvent(motionEvent);
           return true;
       });

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
        bri_layout=findViewById(R.id.brightness_gesture_layout);
        vol_layout=findViewById(R.id.volume_gesture_layout);
        volProgress=findViewById(R.id.volume_progress_bar);
        seek_text=findViewById(R.id.seek_text);
        gestureDetector=new GestureDetector(this,new MyGestureListener());
        briProgress=findViewById(R.id.brightness_progress_bar);
        volume_text=findViewById(R.id.volume_progress_text);
        brightness_text=findViewById(R.id.brightness_progress_text);
        zoomLayout=findViewById(R.id.zoomLayout);
        layout_main=findViewById(R.id.main_layout);
        progressBar = findViewById(R.id.exo_mid_progress);
        trackSelector = new DefaultTrackSelector(this);
        nextBtn = findViewById(R.id.exo_next_btn);
        prevBtn = findViewById(R.id.exo_prev);
        backBtn = findViewById(R.id.video_back);
        title = findViewById(R.id.video_title);
        audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        scalingBtn = findViewById(R.id.scaling);
        lockBtn = findViewById(R.id.lock);
        unlockBtn = findViewById(R.id.unlock);
        root = findViewById(R.id.root_layout);
        audioTrack = findViewById(R.id.audio_track);
        subTitleTrack=findViewById(R.id.exo_subtitle_track);
        position = getIntent().getIntExtra("position", 1);
        playerVideos = Objects.requireNonNull(getIntent().getExtras()).getParcelableArrayList("videoArrayList");
        nextBtn.setOnClickListener(view -> PlayNext());
        prevBtn.setOnClickListener(view -> PlayPrev());
        backBtn.setOnClickListener(view -> finish());
        lockBtn.setOnClickListener(view -> {
            controlsMode = ControlsMode.LOCK;
            root.setVisibility(View.INVISIBLE);
            unlockBtn.setVisibility(View.VISIBLE);
            isControlLocked=true;

        });
        unlockBtn.setOnClickListener(view -> {
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            unlockBtn.setVisibility(View.INVISIBLE);
            isControlLocked=false;
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
    }
    @SuppressLint("ClickableViewAccessibility")
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
        progressBar.setVisibility(View.VISIBLE);
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
        progressBar.setVisibility(View.GONE);
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == Player.STATE_ENDED) {
                    if (position == playerVideos.size() - 1) {
                        String key = playerVideos.get(position).getVideoPath();
                        Long currentPosition = Math.min(0, player.getContentPosition());
                        preferences.setLong(key, currentPosition);
                        position = 0;
                        finish();
                    } else {
                        String key = playerVideos.get(position).getVideoPath();
                        Long currentPosition = Math.min(0, player.getContentPosition());
                        preferences.setLong(key, currentPosition);
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

        if (playerView != null) {
            playerView.onResume();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (player == null) {
            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }

        }
        player.setPlayWhenReady(true);
        if (brightness!=0) adjustBrightness(brightness);
        super.onResume();
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

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private float initialX = 0;
        private float initialY = 0;
        @Override
        public boolean onDown(MotionEvent e) {
            initialX = e.getX();
            initialY = e.getY();
            return true;
        }

        @Override
        public void onShowPress(@NonNull MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
            if (playerView.isControllerFullyVisible()) {
                playerView.hideController();
            } else {
                playerView.showController();
            }
            return true;
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isControlLocked) {
                gestureScrollY += distanceY;
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaX) > Math.abs(deltaY)&&bri_layout.getVisibility()==View.GONE&&vol_layout.getVisibility()==View.GONE) {
                    // Horizontal scroll (seeking)
                    float deltaPercent = deltaX / 500000f;
                    seek_text.setVisibility(View.VISIBLE);
                    seekPlayerPosition(deltaPercent);
                } else {
                    if (Math.abs(gestureScrollY)>SCROLL_STEP) {
                        float velocityY = (e2.getY() - e1.getY()) / (e2.getEventTime() - e1.getEventTime());
                        if (Math.abs(velocityY) > 0.3f) {

                            // Vertical scroll
                            if (e1.getX() < playerView.getWidth() / 2) {
                                // Left half of the screen (brightness)
                                bri_layout.setVisibility(View.VISIBLE);
                                final boolean increase = distanceY > 0;
                                final int newValue;
                                if (increase) newValue = brightness + 1;
                                else newValue = brightness - 1;
                                if (newValue > 0 && newValue <= 16) {
                                    brightness = newValue;
                                }
                                adjustBrightness(brightness);

                            } else {
                                int maxVolume = 0;
                                vol_layout.setVisibility(View.VISIBLE);
                                if (audioManager != null) {
                                    maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                }
                                final boolean increase = distanceY > 0;
                                final int newValue;
                                if (increase) newValue = volume + 1;
                                else newValue = volume - 1;
                                if (newValue > 0 && newValue <= maxVolume) {
                                    volume = newValue;
                                }
                                adjustVolume(volume);


                            }
                        }
                    }
                }
            }

            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    }

    @SuppressLint("SetTextI18n")
    private void seekPlayerPosition(float deltaPercent) {
        // Calculate the new position based on the deltaPercent and update player position
        long duration = player.getDuration();
        long newPosition = (long) (player.getCurrentPosition() + deltaPercent * duration);
        if (newPosition>player.getCurrentPosition()){
            seek_text.setText(String.valueOf(deltaPercent));
        }

        player.seekTo(Math.max(0, Math.min(newPosition, duration)));
    }

    private void adjustBrightness(int value) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = levelToBrightness(value) ;
        getWindow().setAttributes(lp);
        String text= String.valueOf(value);
        brightness_text.setText(text);
        briProgress.setProgress(value);


    }
    float levelToBrightness(final int level) {
        final double d = 0.064 + 0.936 / (double) 128 * (double) level*8;
        return (float) (d * d);
    }


    private void adjustVolume(int volume) {
        if (audioManager!=null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            volProgress.setProgress(volume);
            volume_text.setText(String.valueOf(volume));
        }
    }

    private void hideProgressBar() {
        if (vol_layout.getVisibility()==View.VISIBLE) {
            vol_layout.setVisibility(View.GONE);
        }
        if (bri_layout.getVisibility()==View.VISIBLE) {
            bri_layout.setVisibility(View.GONE);
        }
        if (seek_text.getVisibility()==View.VISIBLE){
            seek_text.setVisibility(View.GONE);
        }
    }

}