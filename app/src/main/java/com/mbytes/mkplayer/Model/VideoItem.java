package com.mbytes.mkplayer.Model;

import android.graphics.Bitmap;

import java.util.Date;
import java.util.Objects;

public class VideoItem implements Comparable<VideoItem>{
    private String videoName;
    private String videoPath;
    private boolean isVideoPlayed;
    private String videoDuration;
    private Date dateAdded;



    public VideoItem(String videoName, String videoPath ,boolean isVideoPlayed,String videoDuration,Date dateAdded) {
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.isVideoPlayed=isVideoPlayed;
        this.videoDuration=videoDuration;
        this.dateAdded=dateAdded;


    }
    public void setVideoPlayed(boolean videoPlayed) {
        isVideoPlayed = videoPlayed;
    }

    public boolean isVideoPlayed() {
        return isVideoPlayed;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
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
        return isVideoPlayed == videoItem.isVideoPlayed && Objects.equals(videoName, videoItem.videoName) && Objects.equals(videoPath, videoItem.videoPath) && Objects.equals(videoDuration, videoItem.videoDuration) && Objects.equals(dateAdded, videoItem.dateAdded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoName, videoPath, isVideoPlayed, videoDuration, dateAdded);
    }

    @Override
    public String toString() {
        return "VideoItem{" +
                "videoName='" + videoName + '\'' +
                ", videoPath='" + videoPath + '\'' +
                ", isVideoPlayed=" + isVideoPlayed +
                ", videoDuration='" + videoDuration + '\'' +
                ", dateAdded=" + dateAdded +
                '}';
    }

    @Override

    public int compareTo(VideoItem otherItem) {
        return this.getDateAdded().compareTo(otherItem.getDateAdded());
    }
}
