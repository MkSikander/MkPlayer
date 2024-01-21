package com.mbytes.mkplayer.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class VideoFolder implements Comparable<VideoFolder>, Serializable {
    private final String folderName;
    private final String folderPath;
    private int videoCount;
    private Date dateAdded;




    public VideoFolder(String folderName, String folderPath, Date dateAdded,int videoCount) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.dateAdded = dateAdded;
        this.videoCount=videoCount;


    }
    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }



    public String getFolderName() {
        return folderName;
    }





    public String getFolderPath() {
        return folderPath;
    }


    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoFolder that = (VideoFolder) o;
        return  Objects.equals(folderName, that.folderName) && Objects.equals(folderPath, that.folderPath) && Objects.equals(dateAdded, that.dateAdded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderName, folderPath, dateAdded);
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoFolder{" +
                "folderName='" + folderName + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", dateAdded=" + dateAdded +
                '}';
    }

    @Override

    public int compareTo(VideoFolder otherFolder) {
        return this.getDateAdded().compareTo(otherFolder.getDateAdded());
    }
}
