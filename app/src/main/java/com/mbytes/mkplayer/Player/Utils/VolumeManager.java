package com.mbytes.mkplayer.Player.Utils;

import android.media.AudioManager;
import android.media.audiofx.LoudnessEnhancer;

public class VolumeManager {

    private AudioManager audioManager;
    private LoudnessEnhancer loudnessEnhancer;

    public VolumeManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public LoudnessEnhancer getLoudnessEnhancer() {
        return loudnessEnhancer;
    }

    public void setLoudnessEnhancer(LoudnessEnhancer value) {
        if (getCurrentVolume() > getMaxStreamVolume()) {
            try {
                if (value != null) {
                    value.setEnabled(true);
                    value.setTargetGain((int) getCurrentLoudnessGain());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loudnessEnhancer = value;
    }

    public int getCurrentStreamVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public int getMaxStreamVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public float getCurrentVolume() {
        return currentVolume;
    }

    private void setCurrentVolume(float volume) {
        currentVolume = volume;
    }

    public float getMaxVolume() {
        return getMaxStreamVolume() * (loudnessEnhancer != null ? 2 : 1);
    }

    private float getCurrentLoudnessGain() {
        return (getCurrentVolume() - getMaxStreamVolume()) * ((float) MAX_VOLUME_BOOST / getMaxStreamVolume());
    }

    public int getVolumePercentage() {
        return (int) ((getCurrentVolume() / getMaxStreamVolume()) * 16);
    }

    @SuppressWarnings("deprecation")
    public void setVolume(float volume, boolean showVolumePanel) {
        setCurrentVolume(Math.min(Math.max(volume, 0), getMaxVolume()));

        if (getCurrentVolume() <= getMaxStreamVolume()) {
            if (loudnessEnhancer != null) {
                loudnessEnhancer.setEnabled(false);
            }

            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    (int) getCurrentVolume(),
                    showVolumePanel && audioManager.isWiredHeadsetOn() ?
                            AudioManager.FLAG_SHOW_UI : 0
            );
        } else {
            try {
                if (loudnessEnhancer != null) {
                    loudnessEnhancer.setEnabled(true);
                    loudnessEnhancer.setTargetGain((int) getCurrentLoudnessGain());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void increaseVolume(boolean showVolumePanel) {
        setVolume(getCurrentVolume() + 1, showVolumePanel);
    }
    public void decreaseVolume(boolean showVolumePanel) {
        setVolume(getCurrentVolume() - 1, showVolumePanel);
    }
    public static final int MAX_VOLUME_BOOST = 2000;
    private float currentVolume;
}

