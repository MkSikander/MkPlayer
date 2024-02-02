package com.mbytes.mkplayer.Player.Utils;

import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.mbytes.mkplayer.Player.PlayerActivity;

public class PlayerGestureHelper implements GestureDetector.OnGestureListener {
    @UnstableApi
    private final PlayerActivity activity;
    private final AudioManager audioManager;
    private final VolumeManager volumeManager;
    private final BrightnessManager brightnessManager;
    private final PlayerView playerView;
    public static final float FULL_SWIPE_RANGE_SCREEN_RATIO = 0.66f;

    @UnstableApi
    public PlayerGestureHelper(PlayerActivity activity,AudioManager audioManager,BrightnessManager brightnessManager,VolumeManager volumeManager){
        this.activity=activity;
        this.audioManager=audioManager;
        this.brightnessManager=brightnessManager;
        this.volumeManager=volumeManager;
        this.playerView=activity.playerView;

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
            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();
            if (Math.abs(deltaX) > Math.abs(deltaY)&&activity.getBriLayoutVisibility()== View.GONE&&activity.getVolLayoutVisibility()==View.GONE) {
                // Horizontal scroll (seeking)
                long seekChange = 0L;
                long seekStart = playerView.getPlayer() != null ? playerView.getPlayer().getCurrentPosition() : 0L;
                playerView.setControllerAutoShow(playerView.isControllerFullyVisible());
                float distanceDiff = Math.min(Math.max(Math.abs(PlayerUtils.pxToDp(distanceX) / 4), 0.5f), 10f);
                activity.setSeekTextVisible();
                long SEEK_STEP_MS = 1000L;
                long change= (long) (distanceDiff* SEEK_STEP_MS);
                long duration=playerView.getPlayer().getDuration();
                if (playerView.getPlayer() != null) {
                    long position;
                    if (distanceX < 0L) {
                        seekChange = (seekChange + change) < duration ? (seekChange + change) : (duration - seekStart);
                        position = Math.min((seekStart + seekChange), duration);
                        playerView.getPlayer().seekTo(position);
                    } else {
                        seekChange = Math.max(seekChange - change,  -seekStart);
                        position = seekStart + seekChange;
                        playerView.getPlayer().seekTo(position);
                    }
                    long currentP=playerView.getPlayer().getCurrentPosition();
                    long seek_value=currentP-activity.getCurrentPP();

                    activity.showSeekInfo(

                            PlayerUtils.formatDurationMillis(currentP),
                            "[" + PlayerUtils.formatDurationMillisSign(seek_value) + "]"
                    );
                    return true;
                }

                return false;

            } else {
                float distanceFull = playerView.getMeasuredHeight() * FULL_SWIPE_RANGE_SCREEN_RATIO;
                float ratioChange = distanceY / distanceFull;
                // Vertical scroll
                if (e1.getX() < (float) playerView.getMeasuredWidth() / 2) {
                    // Left half of the screen (brightness)
                    activity.setBriVisible();
                    float maxBrightness= brightnessManager.maxBrightness;
                    float change=ratioChange*maxBrightness;
                    brightnessManager.setBrightness(brightnessManager.currentBrightness+change);
                    activity.showBriGestureLayout();

                } else {
                    int maxVolume = 0;
                    activity.setVolVisible();
                    if (audioManager != null) {
                        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    }
                    float change=ratioChange*maxVolume;
                    assert audioManager != null;
                    int volume = (int) (volumeManager.getCurrentStreamVolume() + change);
                    volumeManager.setVolume(volume,false);
                    activity.showVolGestureLayout();


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
