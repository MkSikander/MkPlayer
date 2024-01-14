package com.mbytes.mkplayer.Model;

import java.util.Date;
import java.util.Objects;

public class VideoFolder implements Comparable<VideoFolder> {
    private String folderName;
    private String folderPath;
    private Date dateAdded;
    private boolean isSelected;


    public VideoFolder(String folderName, String folderPath, Date dateAdded) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.dateAdded = dateAdded;
        this.isSelected = false; // Default to not selected
    }

    // Constructor with isSelected parameter
    public VideoFolder(String folderName, String folderPath, Date dateAdded, boolean isSelected) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.dateAdded = dateAdded;
        this.isSelected = isSelected;
    }


    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
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
        return isSelected == that.isSelected && Objects.equals(folderName, that.folderName) && Objects.equals(folderPath, that.folderPath) && Objects.equals(dateAdded, that.dateAdded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderName, folderPath, dateAdded, isSelected);
    }

    @Override
    public String toString() {
        return "VideoFolder{" +
                "folderName='" + folderName + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", dateAdded=" + dateAdded +
                ", isSelected=" + isSelected +
                '}';
    }

    @Override

    public int compareTo(VideoFolder otherFolder) {
        return this.getDateAdded().compareTo(otherFolder.getDateAdded());
    }
}
