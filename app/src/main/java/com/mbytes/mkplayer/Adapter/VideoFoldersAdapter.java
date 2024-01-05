package com.mbytes.mkplayer.Adapter;


import static com.google.common.io.Files.getFileExtension;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.VideosListActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> {

    private final List<VideoFolder> videoFolders;
    private ArrayList<String> fpath;

    public VideoFoldersAdapter(List<VideoFolder> videoFolders,ArrayList<String> fpath) {
        this.videoFolders = videoFolders;
        this.fpath=fpath;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define views in the ViewHolder
        public TextView folderNameTextView,folderVideoCount;

        public ViewHolder(View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folder_name);
            folderVideoCount=itemView.findViewById(R.id.folder_video_count);
            // Add other views if needed
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View view = inflater.inflate(R.layout.item_video_folder, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        VideoFolder videoFolder = videoFolders.get(position);

        // Set item views based on the data model
        holder.folderNameTextView.setText(videoFolder.getFolderName());
        holder.folderVideoCount.setText(String.valueOf(noOfFiles(fpath.get(position))));

        // Bind other data if needed

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle item click
                Context context = view.getContext();
                Intent intent = new Intent(context, VideosListActivity.class);
                intent.putExtra("folderPath", videoFolder.getFolderPath());
                intent.putExtra("nameOfFolder", videoFolder.getFolderName());
                context.startActivity(intent);
            }
        });
    }


    private int noOfFiles(String folderPath) {
        int fileCount = 0;

        File folder = new File(folderPath);


        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {

                    if (isVideoFile(file.getPath())) {
                        Log.d("Is Video File", "File: " +file.getPath()+ ", Result: ");
                        fileCount++;

                    }
                }
            }
        }

        return fileCount;
    }



    private boolean isVideoFile(String filePath) {
        // Check by file extension
        if (isVideoFileByExtension(filePath)) {
            return true;
        }

        // Check by MIME type
        if (isVideoFileByMimeType(filePath)) {

            return true;
        }

        return false;
    }

    private boolean isVideoFileByExtension(String filePath) {
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

    private boolean isVideoFileByMimeType(String filePath) {
        String mimeType = getMimeType(filePath);

        // Check if the MIME type indicates a video file
        return mimeType != null && mimeType.startsWith("video/");
    }

    private String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filePath.substring(lastDotIndex + 1);
        }
        return "";
    }

    private String getMimeType(String filePath) {
        String extension = getFileExtension(filePath);

        if (extension.length() > 0) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return null;
    }


    @Override
    public int getItemCount() {
        return videoFolders.size();
    }
}
