package com.mbytes.mkplayer.Player.Utils;


import android.view.WindowManager;
import androidx.media3.common.util.UnstableApi;

import com.mbytes.mkplayer.Player.PlayerActivity;

public class BrightnessManager {

    @UnstableApi
    private PlayerActivity activity;
    public float currentBrightness;
    public float changedBrightness;
    public final float maxBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    @UnstableApi
    public BrightnessManager(PlayerActivity activity) {
        this.activity = activity;
        this.currentBrightness = activity.getCurrentBrightness();
        changedBrightness=currentBrightness;

    }

    public float getCurrentBrightness() {
        return currentBrightness;
    }

    public int getBrightnessPercentage() {
        return (int) ((currentBrightness / maxBrightness) * 16);
    }
    public float getChangedBrightness(){
        return changedBrightness;
    }
    public float getMaxBrightness(){
        return maxBrightness;
    }
    @UnstableApi
    public void setBrightness(float brightness) {
        currentBrightness = Math.min(Math.max(brightness, 0f), maxBrightness);
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = currentBrightness;
        changedBrightness=currentBrightness;
        activity.getWindow().setAttributes(layoutParams);
        activity.setCurrentBrightnessPref();

        // fixes a bug which makes the action bar reappear after changing the brightness

    }
}

