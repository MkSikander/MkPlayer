package com.mbytes.mkplayer.Model;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import java.util.Date;
import java.util.Objects;

public class VideoItem implements Comparable<VideoItem>, Parcelable {
    private final String videoName;
    private final String videoPath;

    private final String videoDuration;
    private Date dateAdded;
    private final String videoType;
    private final String videoResolution;
    private final long videoSize;

    public VideoItem(String videoName, String videoPath ,String videoDuration,Date dateAdded,long videoSize,String videoType,String videoResolution) {
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.videoDuration=videoDuration;
        this.dateAdded=dateAdded;
        this.videoSize=videoSize;
        this.videoResolution=videoResolution;
        this.videoType=videoType;
    }
    public VideoItem(String videoPath, String videoDuration) {
        this.videoPath=videoPath;
        this.videoDuration=videoDuration;
        this.videoName = ""; // Default value for videoName
        this.dateAdded = new Date(); // Default value for dateAdded
        this.videoSize = 0; // Default value for videoSize
        this.videoType = ""; // Default value for videoType
        this.videoResolution = ""; // Default value for videoResolution
    }
    protected VideoItem(Parcel in) {
        videoName = in.readString();
        videoPath = in.readString();
        videoDuration = in.readString();
        videoType = in.readString();
        videoResolution = in.readString();
        videoSize = in.readLong();
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };




    public String getVideoType() {
        return videoType;
    }

    public String getVideoResolution() {
        return videoResolution;
    }



    public long getVideoSize() {
        return videoSize;
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



    public String getVideoName() {
        return videoName;
    }



    public String getVideoPath() {
        return videoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoItem videoItem = (VideoItem) o;
        return videoSize == videoItem.videoSize && Objects.equals(videoName, videoItem.videoName) && Objects.equals(videoPath, videoItem.videoPath) && Objects.equals(videoDuration, videoItem.videoDuration) && Objects.equals(dateAdded, videoItem.dateAdded) && Objects.equals(videoType, videoItem.videoType) && Objects.equals(videoResolution, videoItem.videoResolution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoName, videoPath, videoDuration, dateAdded, videoType, videoResolution, videoSize);
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoItem{" +
                "videoName='" + videoName + '\'' +
                ", videoPath='" + videoPath + '\'' +
                ", videoDuration='" + videoDuration + '\'' +
                ", dateAdded=" + dateAdded +
                ", videoType='" + videoType + '\'' +
                ", videoResolution='" + videoResolution + '\'' +
                ", videoSize=" + videoSize +
                '}';
    }

    @Override

    public int compareTo(VideoItem otherItem) {
        return this.getDateAdded().compareTo(otherItem.getDateAdded());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(videoName);
        parcel.writeString(videoPath);
        parcel.writeString(videoDuration);
        parcel.writeString(videoType);
        parcel.writeString(videoResolution);
        parcel.writeLong(videoSize);
    }
}
