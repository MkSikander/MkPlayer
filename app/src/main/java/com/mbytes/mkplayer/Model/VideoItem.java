package com.mbytes.mkplayer.Model;

import android.graphics.Bitmap;

import java.util.Objects;

public class VideoItem {
    private String videoName;
    private String videoPath;


    public VideoItem(String videoName, String videoPath) {
        this.videoName = videoName;
        this.videoPath = videoPath;

    }


    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoItem videoItem = (VideoItem) o;
        return Objects.equals(videoName, videoItem.videoName) && Objects.equals(videoPath, videoItem.videoPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoName, videoPath);
    }

    @Override
    public String toString() {
        return "VideoItem{" +
                "videoName='" + videoName + '\'' +
                ", videoPath='" + videoPath + '\'' +

                '}';
    }
}
