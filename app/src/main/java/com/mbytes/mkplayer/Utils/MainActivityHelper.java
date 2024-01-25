package com.mbytes.mkplayer.Utils;

import android.webkit.MimeTypeMap;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mbytes.mkplayer.Model.VideoItem;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivityHelper {

    public static boolean isVideoFile(String filePath) {
        // Check by file extension
        if (isVideoFileByExtension(filePath)) {
            return true;
        }
        // Check by MIME type
        return isVideoFileByMimeType(filePath);
    }

    private static boolean isVideoFileByExtension(String filePath) {
        String[] videoExtensions = {"3g2", "3gp","aaf", "asf", "avchd", "avi", "drc", "flv", "m2v", "m3u8", "m4p", "m4v", "mkv", "mng", "mov", "mp2", "mp4", "mpe", "mpeg","mpeg", "mpg", "mpv", "mxf", "nsv", "ogg", "ogv", "qt", "rm", "rmvb", "roq", "svi", "vob", "webm", "wmv", "yuv"};
        // Get the file extension
        String fileExtension = getFileExtension(filePath);
        // Check if the file extension is in the list of video extensions
        for (String videoExt : videoExtensions) {
            if (videoExt.equalsIgnoreCase(fileExtension)) {

                return true;
            }
        }
        return false;
    }
    private static boolean isVideoFileByMimeType(String filePath) {
        String mimeType = getMimeType(filePath);

        // Check if the MIME type indicates a video file
        return mimeType != null && mimeType.startsWith("video/");
    }

    private static String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filePath.substring(lastDotIndex + 1);
        }
        return "";
    }
    private static String getMimeType(String filePath) {
        String extension = getFileExtension(filePath);

        if (!extension.isEmpty()) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return null;
    }

   public static ArrayList<VideoItem> convertJsonToGson(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<VideoItem>>(){}.getType();
        return gson.fromJson(json, type);
    }

}
