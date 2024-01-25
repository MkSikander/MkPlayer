package com.mbytes.mkplayer.Utils;

import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;

import com.google.gson.Gson;
import com.mbytes.mkplayer.Model.VideoItem;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerUtils {

    //getting video Orientation
    public static int getVideoRotation(String videoPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            // Swap width and height for portrait videos
            int width = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
            int height = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            if (width > height) {
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
    public static String convertVideoListToJson(ArrayList<VideoItem> videoList) {
        Gson gson = new Gson();
        return gson.toJson(videoList);
    }

}
