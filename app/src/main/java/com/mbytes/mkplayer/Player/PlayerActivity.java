package com.mbytes.mkplayer.Player;

import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.convertVideoListToJson;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.setAudioTrack;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.setPlaybackSpeed;
import static com.mbytes.mkplayer.Player.Utils.PlayerUtils.setSubTrack;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.AudioAttributes;
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
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.Utils.BrightnessManager;
import com.mbytes.mkplayer.Player.Utils.PlayerGestureHelper;
import com.mbytes.mkplayer.Player.Utils.PlaylistVideoAdapter;
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
    public static boolean isControlLocked = false;
    public static boolean isPlaylistVisible=false;
    private boolean startAutoPlay;
    private int startItemIndex;
    private long startPosition;
    private TrackSelectionParameters trackSelectionParameters;


    public static final Long[] seekIncrementTime = {10000L, 15000L, 20000L, 25000L, 30000L, 35000L, 40000L, 45000L, 50000L};
    private static final String KEY_TRACK_SELECTION_PARAMETERS = "track_selection_parameters";
    private static final String KEY_ITEM_INDEX = "item_index";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private ArrayList<VideoItem> playerVideos = new ArrayList<>();
    private TextView title;
    private TextView volume_text;
    private TextView brightness_text;
    private TextView seek_duration;
    private TextView seek_change;
    private DefaultTrackSelector trackSelector;
    private Preferences preferences;
    private static final float[] playbackSpeeds = {0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2.0f};
    private int position;
    private ImageView unlockBtn;
    private ImageView audioTrack;
    private ImageView subTitleTrack;
    private ImageView playbackSpeed;
    private ImageView playList;
    private ImageView rotate;
    //Gesture Related import
    private ProgressBar progressBar, volProgress, briProgress;
    private LinearLayout bri_layout, vol_layout, startOverLayout;
    private RelativeLayout root;
    private BrightnessManager brightnessManager;
    private VolumeManager volumeManager;
    private PlayerGestureHelper playerGestureHelper;
    private AspectRatioFrameLayout zoomLayout;
    private Long skipPosition = 0L;
    private int tempPlaybackSpeed;

    private static boolean orientation;
    private RecyclerView playListRecyclerViewLand, playListRecyclerViewPortrait;
    private ConstraintLayout playlistLayoutLand, playlistLayoutPortrait;
    private Animation slideInBottom, slideOutBottom, slideInRight, slideOutRight;


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

    @SuppressLint("SetTextI18n")
    private void initViews(Bundle savedInstanceState) {
        preferences = new Preferences(PlayerActivity.this);
        tempPlaybackSpeed = preferences.getDefaultPlaybackSpeed();
        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.exo_mid_progress);
        trackSelector = new DefaultTrackSelector(this);
        ImageView nextBtn = findViewById(R.id.exo_next_btn);
        playListRecyclerViewPortrait = findViewById(R.id.playlist_recycler_view_portrait);
        playListRecyclerViewLand = findViewById(R.id.playlist_recycler_view_land);
        playlistLayoutLand = findViewById(R.id.playlist_layout_land);
        playlistLayoutPortrait = findViewById(R.id.playlist_layout_portrait);
        slideInBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);
        slideOutBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_bottom);
        slideInRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right);
        slideOutRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right);
        ImageView playlistBackLand = findViewById(R.id.btn_back_land);
        ImageView playlistBackPortrait = findViewById(R.id.btn_back_portrait);
        ImageView prevBtn = findViewById(R.id.exo_prev);
        zoomLayout = findViewById(R.id.exo_content_frame);
        ImageView backBtn = findViewById(R.id.video_back);
        title = findViewById(R.id.video_title);
        ImageView scalingBtn = findViewById(R.id.scaling);
        rotate = findViewById(R.id.rotate);
        ImageView lockBtn = findViewById(R.id.lock);
        unlockBtn = findViewById(R.id.unlock);
        TextView startOver = findViewById(R.id.start_over);
        ImageView cancelStartOver = findViewById(R.id.start_over_cancel);
        startOverLayout = findViewById(R.id.start_over_layout);
        root = findViewById(R.id.root_layout);
        audioTrack = findViewById(R.id.audio_track);
        subTitleTrack = findViewById(R.id.exo_subtitle_track);
        playbackSpeed = findViewById(R.id.playback_speed);
        playList = findViewById(R.id.playlist);
        bri_layout = findViewById(R.id.brightness_gesture_layout);
        vol_layout = findViewById(R.id.volume_gesture_layout);
        volProgress = findViewById(R.id.volume_progress_bar);
        seek_duration = findViewById(R.id.seek_duration);
        seek_change = findViewById(R.id.seek_change);
        briProgress = findViewById(R.id.brightness_progress_bar);
        volume_text = findViewById(R.id.volume_progress_text);
        brightness_text = findViewById(R.id.brightness_progress_text);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        volumeManager = new VolumeManager(audioManager);
        brightnessManager = new BrightnessManager(this);
        int newPosition = getIntent().getIntExtra("VIDEO_POSITION", -1);
        if (newPosition > -1) {
            playThis(newPosition);
        }
        try {
            Intent intent = getIntent();
            if (intent.getData() != null && intent.getData().getScheme() != null &&
                    intent.getData().getScheme().equalsIgnoreCase("content")) {
                final Cursor cursor = getContentResolver().query(intent.getData(),
                        new String[]{(MediaStore.Video.Media.DATA)},
                        null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    try {
                        final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                        final File file = new File(path);
                        final VideoItem videoItem = new VideoItem(path, "0", file.getName());
                        playerVideos.add(videoItem);
                        cursor.close();
                    } catch (Exception e) {
                        final String tempPath = getPathFromURI(this, intent.getData());
                        final File tempFile = new File(tempPath);
                        final VideoItem video = new VideoItem(tempPath, "0L", tempFile.getName());
                        playerVideos.add(video);
                        cursor.close();
                    }
                }
                position = 0;
            } else {
                position = getIntent().getIntExtra("position", 1);
                playerVideos = Objects.requireNonNull(getIntent().getExtras()).getParcelableArrayList("videoArrayList");
            }
        }
        catch (Exception ignored){}

        playerGestureHelper = new PlayerGestureHelper(this, brightnessManager, volumeManager);

        nextBtn.setOnClickListener(view -> {
            PlayNext();
            setCurrentPosition();
        });
        prevBtn.setOnClickListener(view -> {
            PlayPrev();
            setCurrentPosition();
        });
        backBtn.setOnClickListener(view -> finish());
        lockBtn.setOnClickListener(view -> {
            root.setVisibility(View.INVISIBLE);
            unlockBtn.setVisibility(View.VISIBLE);
            isControlLocked = true;
        });
        unlockBtn.setOnClickListener(view -> {
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
            TextView zoomText=findViewById(R.id.zoom_perc);
            zoomText.setVisibility(View.VISIBLE);
            // Default to fit
            switch (currentMode) {
                case RESIZE_MODE_FIT:
                    playerView.setResizeMode(RESIZE_MODE_FILL);
                    zoomText.setText("Stretch");
                    scalingBtn.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_zoom_fill));
                    break;
                case RESIZE_MODE_FILL:
                    playerView.setResizeMode(RESIZE_MODE_ZOOM);
                    zoomText.setText("Crop");
                    scalingBtn.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_zoom_zoom));
                    break;
                case RESIZE_MODE_ZOOM:
                    playerView.setResizeMode(RESIZE_MODE_FIT);
                    zoomText.setText("100 %");
                    scalingBtn.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_zoom_fixed_height));
                    break;
                case RESIZE_MODE_FIXED_HEIGHT:
                case RESIZE_MODE_FIXED_WIDTH:
                    playerView.setResizeMode(RESIZE_MODE_FIT);
                    zoomText.setText("100 %");
                    scalingBtn.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_fit_screen));
                    break;
            }
            new Handler().postDelayed(()-> zoomText.setVisibility(View.GONE),400);

        });
        startOver.setOnClickListener(view -> {
            player.seekTo(0L);
            startOverLayout.setVisibility(View.GONE);
        });
        cancelStartOver.setOnClickListener(view -> startOverLayout.setVisibility(View.VISIBLE));
        playlistBackLand.setOnClickListener(view -> hidePlaylist());
        playlistBackPortrait.setOnClickListener(view -> hidePlaylist());

    }



    @SuppressLint("SourceLockedOrientationActivity")
    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (preferences.getResumePref()) {
            skipPosition = preferences.getLong(playerVideos.get(position).getVideoPath());
        }
        if (preferences.getBrightnessPref()) {
            float brightness = preferences.getPreviousBrightnessPref();
            if (brightness == -2f) {
                brightness = getIntent().getFloatExtra("bri", 0);
            }
            brightnessManager.setBrightness(brightness);
        }
        setOrientation();
        String path = playerVideos.get(position).getVideoPath();
        title.setText(playerVideos.get(position).getVideoName());
        int seekIndex = preferences.getDefaultSeekSpeed();
        DefaultExtractorsFactory extractorsFactory =
                new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
        if (player == null) {
            int playbackSpeedIndex = preferences.getDefaultPlaybackSpeed();
            player = new ExoPlayer.Builder(this).setTrackSelector(trackSelector)
                    .setMediaSourceFactory(new DefaultMediaSourceFactory(this, extractorsFactory))
                    .setHandleAudioBecomingNoisy(true)
                    .setSeekForwardIncrementMs(seekIncrementTime[seekIndex])
                    .setSeekBackIncrementMs(seekIncrementTime[seekIndex])
                    .setAudioAttributes(getAudioAttributes(), true)
                    .build();
            player.setPlaybackSpeed(playbackSpeeds[playbackSpeedIndex]);
            playerView.setPlayer(player);

        }
        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(path)));
        player.setMediaItem(mediaItem);
        if (preferences.getFastSeekPref()) {
            player.setSeekParameters(SeekParameters.NEXT_SYNC);
        } else {
            player.setSeekParameters(SeekParameters.DEFAULT);
        }
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
        player.prepare();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                switch (playbackState) {
                    case Player.STATE_READY: {
                        player.play();
                        progressBar.setVisibility(View.GONE);
                        break;
                    }
                    case Player.STATE_BUFFERING: {
                        progressBar.setVisibility(View.GONE);
                        break;
                    }
                    case Player.STATE_ENDED: {
                        String key = playerVideos.get(position).getVideoPath();
                        Long currentPosition = 0L;
                        preferences.setLong(key, currentPosition);
                        if (position == playerVideos.size() - 1) {
                            position = 0;
                            finish();
                        } else {
                            if (preferences.getAutoPlayPref()) {
                                PlayNext();
                            } else {
                                player.pause();
                            }
                        }
                        break;
                    }
                    case Player.STATE_IDLE: {
                        break;
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
        playbackSpeed.setOnClickListener(view -> setPlaybackSpeed(player, PlayerActivity.this));
        playList.setOnClickListener(view -> showPlaylistVideos());

        String videoPath = playerVideos.get(position).getVideoPath();
        if (!getVideoPlayedStatus(videoPath)) {
            setVideoPlayedStatus(videoPath);
            preferences.setIsAnyVideoPlayed(true);
        }
        rotate.setOnClickListener(view -> {
            if (orientation) {
                orientation = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                orientation = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        });
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void setOrientation() {
        int defOrient = preferences.getDefaultOrientation();
        switch (defOrient) {
            case 0: {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            }
            case 1: {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            }
            case 2: {
                setRequestedOrientation(PlayerUtils.getVideoRotation(playerVideos.get(position).getVideoPath()));
                break;
            }
        }

    }

    private AudioAttributes getAudioAttributes() {
        return new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
    }

    private void showStartOverLayout() {
        Handler startOverShow = new Handler();
        Handler startOverHide = new Handler();
        //show StartOverLayout
        startOverShow.postDelayed(() -> startOverLayout.setVisibility(View.VISIBLE), 1000);
        startOverHide.postDelayed(() -> startOverLayout.setVisibility(View.GONE), 6000);
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

    public void playThis(int newPosition) {
        try {
            setCurrentPosition();
            player.stop();
            position = newPosition;
            initializePlayer();
        } catch (Exception ignored) {
        }
    }
    public void updateList(ArrayList<VideoItem> videosList) {
        playerVideos = videosList;
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
            setCurrentPosition();
            setCurrentBrightnessPref();
            player.release();
            player = null;
            playerView.setPlayer(/* player= */ null);
        }
    }

    private void setCurrentPosition() {
        String key = playerVideos.get(position).getVideoPath();
        Long currentPosition = Math.max(0, player.getContentPosition());
        preferences.setLong(key, currentPosition);

    }

    //for bundle instances To resume video where it was left (during Calls and Interrupt)
    private void updateTrackSelectorParameters() {
        if (player != null) {
            trackSelectionParameters = player.getTrackSelectionParameters();
        }
    }

    public void setCurrentBrightnessPref() {
        preferences.setCurrentBrightnessPref(brightnessManager.getChangedBrightness());
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
        initializePlayer();
        if (playerView != null) {
            playerView.onResume();
        }
        player.setPlayWhenReady(true);
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
        preferences.setDefaultPlaybackSpeed(tempPlaybackSpeed);
        setLastVideos();
        super.onStop();
    }
    @Override
    public void onDestroy() {
        preferences.setDefaultPlaybackSpeed(tempPlaybackSpeed);
        releasePlayer();
        super.onDestroy();
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
        TextView fText=findViewById(R.id.lp_fast_seek);
        if (player.getPlaybackParameters().speed!=preferences.getDefaultPlaybackSpeed()){
            player.setPlaybackSpeed(playbackSpeeds[preferences.getDefaultPlaybackSpeed()]);
            fText.setVisibility(View.GONE);

        }
        Handler handler = new Handler();
        handler.postDelayed(() -> {
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
        }, 800);
    }

    public float getCurrentBrightness() {
        try {
            int systemBri = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            return systemBri / 255.0f;
        } catch (Exception e) {
            return -1.0f;
        }
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

    public static void setOrientationBool(int a) {
        orientation = a == 6;
    }

    public void setStartOverVisibility() {
        startOverLayout.setVisibility(View.GONE);
    }

    private void showPlaylistVideos() {
        isPlaylistVisible=true;
        if (playerView.isControllerFullyVisible()) {
            playerView.hideController();
        }
        bri_layout.setVisibility(View.GONE);
        vol_layout.setVisibility(View.GONE);

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            playlistLayoutLand.getLayoutParams().width= playerView.getMeasuredWidth()/2;
            playlistLayoutLand.requestLayout();
            playlistLayoutLand.setVisibility(View.VISIBLE);
            playlistLayoutLand.startAnimation(slideInRight);
            playListRecyclerViewLand.setLayoutManager(new LinearLayoutManager(this));
            PlaylistVideoAdapter adapter = new PlaylistVideoAdapter(this, playerVideos, position);
            playListRecyclerViewLand.setAdapter(adapter);
            playListRecyclerViewLand.scrollToPosition(position);
        } else {
            playlistLayoutPortrait.setVisibility(View.VISIBLE);
            playlistLayoutPortrait.startAnimation(slideInBottom);
            playListRecyclerViewPortrait.setLayoutManager(new LinearLayoutManager(this));
            PlaylistVideoAdapter adapter = new PlaylistVideoAdapter(this, playerVideos, position);
            playListRecyclerViewPortrait.setAdapter(adapter);
            playListRecyclerViewPortrait.scrollToPosition(position);
        }
    }

    public void hidePlaylist() {
        isPlaylistVisible=false;
        if (playlistLayoutPortrait.getVisibility() == View.VISIBLE) {
            playlistLayoutPortrait.startAnimation(slideOutBottom);
            playlistLayoutPortrait.setVisibility(View.GONE);
        }
        if(playlistLayoutLand.getVisibility()==View.VISIBLE){
            playlistLayoutLand.startAnimation(slideOutRight);
            playlistLayoutLand.setVisibility(View.GONE);
        }

    }
    private String getPathFromURI(Context context, Uri uri) {
        String filePath = "";
        // ExternalStorageProvider
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath()  + "/" + split[1];
        } else {
            // getExternalMediaDirs() added in API 21
            File[] external = context.getExternalMediaDirs();
            if (external.length > 1) {
                filePath = external[1].getAbsolutePath();
                filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
            }
            return filePath; // Updated return statement
        }
    }



}