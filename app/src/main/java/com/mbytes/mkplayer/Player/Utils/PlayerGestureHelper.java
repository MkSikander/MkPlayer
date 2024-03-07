package com.mbytes.mkplayer.Player.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;

import java.util.Objects;

@OptIn(markerClass = UnstableApi.class)
public class PlayerGestureHelper extends GestureDetector.SimpleOnGestureListener{
    @UnstableApi
    private final PlayerActivity activity;
    private final VolumeManager volumeManager;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector zoomGestureDetector;
    private final BrightnessManager brightnessManager;
    private final PlayerView playerView;
    private static  float scaleFactor;
    private final TextView zoomPercent,seekForward,seekBack,fastSeekText;
    @UnstableApi
    private final AspectRatioFrameLayout zoomLayout;
    private long prevP;
    public static final float EXCLUSION_AREA=20f;
    private static final int[] seekIncrementTime={10,15,20,25,30,35,40,45,50};

    public static final float FULL_SWIPE_RANGE_SCREEN_RATIO = 0.66f;
    private final Preferences preferences;
    private int dpSeekForward,dpSeekBack;


    @SuppressLint("ClickableViewAccessibility")
    public PlayerGestureHelper(PlayerActivity activity,BrightnessManager brightnessManager,VolumeManager volumeManager){
        this.activity=activity;
        this.brightnessManager=brightnessManager;
        this.volumeManager=volumeManager;
        this.playerView=activity.playerView;
        preferences=new Preferences(playerView.getContext());
        zoomLayout=activity.findViewById(R.id.exo_content_frame);
        zoomPercent=activity.findViewById(R.id.zoom_perc);
        seekForward=activity.findViewById(R.id.dp_seek_forward);
        seekBack=activity.findViewById(R.id.dp_seek_back);
        fastSeekText=activity.findViewById(R.id.lp_fast_seek);
        scaleFactor=zoomLayout.getScaleY();
        gestureDetector=new GestureDetector(activity,this);
        zoomGestureDetector=new ScaleGestureDetector(activity,new scaleGestureDetector());
        onTouchListener(playerView,activity);
    }

   private class scaleGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener{
       @SuppressLint("SetTextI18n")
       @Override
       public boolean onScale(@NonNull ScaleGestureDetector detector) {
           if(!activity.isControlLocked()&&preferences.getZoomGesture()) {

               scaleFactor *=detector.getScaleFactor();
               zoomPercent.setVisibility(View.VISIBLE);
               scaleFactor = Math.max(0.25f, Math.min(scaleFactor, 4f));
               zoomLayout.setScaleX(scaleFactor);
               zoomLayout.setScaleY(scaleFactor);
               zoomPercent.setText(" " + (int) (scaleFactor * 100) + " % ");
           }
           return true;
       }

       @Override
       public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
           Handler mHandler=new Handler();
           mHandler.postDelayed(() -> zoomPercent.setVisibility(View.GONE),1000);
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

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        float screenWidth = playerView.getWidth();
        float tapX = e.getX();
        if (inExclusionArea(e)&&!PlayerActivity.isControlLocked&&!PlayerActivity.isPlaylistVisible) {
            if (activity.playerView.isControllerFullyVisible()) {
                activity.playerView.hideController();
            }
            if (tapX < screenWidth / 2) {
                seekForward.setVisibility(View.GONE);
                dpSeekBack+=seekIncrementTime[preferences.getDefaultSeekSpeed()];
                // Double tap on the left side, rewind
                Objects.requireNonNull(playerView.getPlayer()).seekBack();
                seekBack.setVisibility(View.VISIBLE);
                seekBack.setText(dpSeekBack+"s");

            } else {
                seekBack.setVisibility(View.GONE);
                dpSeekForward+=seekIncrementTime[preferences.getDefaultSeekSpeed()];
                // Double tap on the right side, forward
                Objects.requireNonNull(playerView.getPlayer()).seekForward();
                seekForward.setVisibility(View.VISIBLE);
                seekForward.setText(dpSeekForward+"s");

            }
            new Handler().postDelayed(this::hideSeekLabel,3000);
        }
        return super.onDoubleTap(e);
    }

    private void hideSeekLabel() {
            seekForward.setVisibility(View.GONE);
            seekBack.setVisibility(View.GONE);
            dpSeekBack=0;
            dpSeekForward=0;

    }


    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent motionEvent) {
        if (activity.playerView.isControllerFullyVisible()) {
            activity.playerView.hideController();
            hideSeekLabel();
        } else {
             activity.playerView.showController();
             hideSeekLabel();
                activity.hidePlaylist();

        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {

        if (!PlayerActivity.isControlLocked &&!PlayerActivity.isPlaylistVisible && inExclusionArea(e1)) {
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
                        activity.setVolVisible();
                        float change = ratioChange * volumeManager.getMaxStreamVolume();
                        volumeManager.setVolume(volumeManager.getCurrentVolume()+change, false);
                        activity.showVolGestureLayout();

                    }
                }
                return true;
            }

        }

        return false;
    }



    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        if (e.getPointerCount()<=1) {
            if (!PlayerActivity.isControlLocked && !PlayerActivity.isPlaylistVisible && inExclusionArea(e)) {
                playerView.hideController();
                fastSeekText.setVisibility(View.VISIBLE);
                Objects.requireNonNull(playerView.getPlayer()).setPlaybackSpeed(2f);
            }

        }

    }

    @Override
    public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
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
    private boolean inExclusionArea(MotionEvent firstEvent) {
        float gestureExclusionBorder = dpToPx(EXCLUSION_AREA, playerView.getContext());
        return !(firstEvent.getY() < gestureExclusionBorder) &&
                !(firstEvent.getY() > playerView.getHeight() - gestureExclusionBorder) &&
                !(firstEvent.getX() < gestureExclusionBorder) &&
                !(firstEvent.getX() > playerView.getWidth() - gestureExclusionBorder);
    }
    public static float dpToPx(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
