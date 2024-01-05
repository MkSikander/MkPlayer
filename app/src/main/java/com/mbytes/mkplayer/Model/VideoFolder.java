package com.mbytes.mkplayer.Model;

public class VideoFolder {
    private String folderName;
    private String folderPath;


    public VideoFolder(String folderName, String folderPath) {
        this.folderName = folderName;
        this.folderPath = folderPath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoFolder that = (VideoFolder) o;

        if (folderName != null ? !folderName.equals(that.folderName) : that.folderName != null) return false;
        return folderPath != null ? folderPath.equals(that.folderPath) : that.folderPath == null;
    }

    @Override
    public int hashCode() {
        int result = folderName != null ? folderName.hashCode() : 0;
        result = 31 * result + (folderPath != null ? folderPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VideoFolder{" +
                "folderName='" + folderName + '\'' +
                ", folderPath='" + folderPath + '\'' +
                '}';
    }
}
