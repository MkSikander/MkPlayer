package com.mbytes.mkplayer.Player.Utils;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;

import java.util.Objects;

public class PlayerGestureHelper implements GestureDetector.OnGestureListener {
    @UnstableApi
    private final PlayerActivity activity;
    private final AudioManager audioManager;
    private final VolumeManager volumeManager;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector zoomGestureDetector;
    private final BrightnessManager brightnessManager;
    private final PlayerView playerView;
    private static  float scaleFactor=1f;
    private TextView zoomPercent;
    private FrameLayout zoomlayout;
    private long prevP;
    public static final float FULL_SWIPE_RANGE_SCREEN_RATIO = 0.66f;
    private Preferences preferences;


    @SuppressLint("ClickableViewAccessibility")
    @UnstableApi
    public PlayerGestureHelper(PlayerActivity activity,AudioManager audioManager,BrightnessManager brightnessManager,VolumeManager volumeManager){
        this.activity=activity;
        this.audioManager=audioManager;
        this.brightnessManager=brightnessManager;
        this.volumeManager=volumeManager;
        this.playerView=activity.playerView;
        preferences=new Preferences(playerView.getContext());
        zoomlayout=activity.findViewById(R.id.zoom_layout);
        zoomPercent=activity.findViewById(R.id.zoom_perc);
        gestureDetector=new GestureDetector(activity,this);
        zoomGestureDetector=new ScaleGestureDetector(activity,new scaleGestureDetector());
        onTouchListener(playerView,activity);


    }
@UnstableApi
   private class scaleGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener{
       @SuppressLint("SetTextI18n")
       @Override
       public boolean onScale(@NonNull ScaleGestureDetector detector) {
           if(!activity.isControlLocked()&&preferences.getZoomGesture()) {
               scaleFactor *= detector.getScaleFactor();
               zoomPercent.setVisibility(View.VISIBLE);
               scaleFactor = Math.max(0.25f, Math.min(scaleFactor, 4f));
               zoomlayout.setScaleX(scaleFactor);
               zoomlayout.setScaleY(scaleFactor);
               zoomPercent.setText(" " + (int) (scaleFactor * 100) + " % ");
           }
           return true;
       }

       @Override
       public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
           Handler mHandler=new Handler();
           mHandler.postDelayed(new Runnable() {
               @Override
               public void run() {
                   zoomPercent.setVisibility(View.GONE);
               }
           },1000);
           super.onScaleEnd(detector);
       }
   }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }
    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {

    }
    @UnstableApi
    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        if (activity.playerView.isControllerFullyVisible()) {
            activity.playerView.hideController();
        } else {
            activity.playerView.showController();
        }
        return true;
    }

    @UnstableApi
    @Override
    public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        boolean isControlLocked=activity.isControlLocked();
        if (!isControlLocked) {
            activity.setStartOverVisibility();
            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();
            if (Math.abs(deltaX) > Math.abs(deltaY)&&activity.getBriLayoutVisibility()== View.GONE&&activity.getVolLayoutVisibility()==View.GONE) {
                // Horizontal scroll (seeking)
                if (preferences.getSeekGesture()) {
                    long seekChange = 0L;
                    long seekStart = playerView.getPlayer() != null ? playerView.getPlayer().getCurrentPosition() : 0L;
                    playerView.setControllerAutoShow(playerView.isControllerFullyVisible());
                    float distanceDiff = Math.min(Math.max(Math.abs(PlayerUtils.pxToDp(distanceX) / 4), 0.5f), 10f);
                    activity.setSeekTextVisible();
                    long SEEK_STEP_MS = 1000L;
                    long change = (long) (distanceDiff * SEEK_STEP_MS);
                    long duration = playerView.getPlayer().getDuration();
                    if (playerView.getPlayer() != null) {
                        long position;
                        if (distanceX < 0L) {
                            seekChange = (seekChange + change) < duration ? (seekChange + change) : (duration - seekStart);
                            position = Math.min((seekStart + seekChange), duration);
                            playerView.getPlayer().seekTo(position);
                        } else {
                            seekChange = Math.max(seekChange - change, -seekStart);
                            position = seekStart + seekChange;
                            playerView.getPlayer().seekTo(position);
                        }
                        long currentP = playerView.getPlayer().getCurrentPosition();
                        long seek_value = currentP - prevP;

                        activity.showSeekInfo(

                                PlayerUtils.formatDurationMillis(currentP),
                                "[" + PlayerUtils.formatDurationMillisSign(seek_value) + "]"
                        );
                        return true;
                    }
                    return false;
                }
                return false;
            } else {
                if (preferences.getScrollGesture()) {
                    float distanceFull = playerView.getMeasuredHeight() * FULL_SWIPE_RANGE_SCREEN_RATIO;
                    float ratioChange = distanceY / distanceFull;
                    // Vertical scroll
                    if (e1.getX() < (float) playerView.getMeasuredWidth() / 2) {
                        // Left half of the screen (brightness)
                        activity.setBriVisible();
                        float maxBrightness = brightnessManager.maxBrightness;
                        float change = ratioChange * maxBrightness;
                        brightnessManager.setBrightness(brightnessManager.getCurrentBrightness() + change);
                        activity.showBriGestureLayout();
                    } else {
                        //Right half of the Screen (volume)
                        int maxVolume = 0;
                        activity.setVolVisible();
                        if (audioManager != null) {
                            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        }
                        float change = ratioChange * maxVolume;
                        assert audioManager != null;
                        int volume = (int) (volumeManager.getCurrentStreamVolume() + change);
                        volumeManager.setVolume(volume, false);
                        activity.showVolGestureLayout();


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
    @UnstableApi
    @SuppressLint("ClickableViewAccessibility")
    private void onTouchListener(PlayerView playerView, PlayerActivity activity) {
        playerView.setOnTouchListener((view, motionEvent) -> {
            int Action = motionEvent.getAction();
            switch (motionEvent.getPointerCount()){
                case 1: {
                    gestureDetector.onTouchEvent(motionEvent);
                }
                case 2:{
                   zoomGestureDetector.onTouchEvent(motionEvent);
                }
            }
            if (Action == MotionEvent.ACTION_UP||motionEvent.getPointerCount()>=3) {
                activity.hideProgressBar();
            } else if (Action == MotionEvent.ACTION_DOWN) {
                if (playerView.getPlayer() != null) {
                    prevP = playerView.getPlayer().getCurrentPosition();
                }
            }
            return true;
        });
    }
    public  void resetScaleFactor(){
        scaleFactor=1f;
    }

}
