package com.mbytes.mkplayer.Adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Activities.VideosListActivity;
import com.mbytes.mkplayer.Utils.FolderUtils;
import com.mbytes.mkplayer.Utils.Preferences;

import java.util.ArrayList;

public class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> implements FolderUtils.AdapterCallback {
    Preferences preferences;
    private final ArrayList<VideoFolder> videoFolders;
    public VideoFoldersAdapter(ArrayList<VideoFolder> videoFolders) {
        this.videoFolders = videoFolders;
    }
    public interface VideoLoadListener {
        void onVideoLoadRequested();
    }
    private VideoLoadListener videoLoadListener;
    public void setVideoLoadListener(VideoLoadListener listener) {
        this.videoLoadListener = listener;
    }

    @Override
    public void onAdapterMethodCalled() {
        if (videoLoadListener != null) {
            videoLoadListener.onVideoLoadRequested();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define views in the ViewHolder
        public TextView folderNameTextView,folderVideoCount,NewVideoCount;
        public ImageView checkImage;


        public ViewHolder(View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folder_name);
            folderVideoCount=itemView.findViewById(R.id.folder_video_count);
            checkImage =itemView.findViewById(R.id.check_mark);
            NewVideoCount=itemView.findViewById(R.id.no_of_new_videos);

            // Add other views if needed
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View view = inflater.inflate(R.layout.item_folder, parent, false);
        preferences=new Preferences(context);
        // Return a new holder instance
        return new ViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        VideoFolder videoFolder = videoFolders.get(position);
        // Set item views based on the data model
        String folderPath=videoFolder.getFolderPath();
        String fname= videoFolder.getFolderName();
        int videoCount=videoFolder.getVideoCount();
        fname=(fname.length() > 25) ? fname.substring(0, 25) + "..." : fname;
        holder.folderNameTextView.setText(fname);
        holder.folderVideoCount.setText(videoCount +" videos");
        // Bind other data if needed
        holder.itemView.setOnClickListener(view -> {
                // Handle item click
                Context context = view.getContext();
                Intent intent = new Intent(context, VideosListActivity.class);
                intent.putExtra("folderPath", videoFolder.getFolderPath());
                intent.putExtra("nameOfFolder", videoFolder.getFolderName());
                context.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(view -> {
            FolderUtils.showMenu(view.getContext(),videoFolder);
            return false;
        });
//        String keyForCount="count_"+folderPath;
//        if (preferences.getInt(keyForCount)<0){
//            preferences.setBoolean("first_run",true);
//            preferences.setInt(folderPath,videoCount);
//            preferences.setInt(keyForCount,videoCount);
//
//        }
//        int noOfNewVideos = preferences.getInt(folderPath);
//        int totalVideos= preferences.getInt(keyForCount);
//        if (noOfNewVideos>0) {
//            if (videoCount == totalVideos) {
//                holder.NewVideoCount.setText(noOfNewVideos);
//                holder.NewVideoCount.setVisibility(View.VISIBLE);
//            }
//            else if (videoCount<totalVideos && noOfNewVideos>=videoCount){
//                preferences.setInt(keyForCount,videoCount);
//                int tempCount=totalVideos-videoCount;
//                preferences.setInt(folderPath,(noOfNewVideos-tempCount));
//                holder.NewVideoCount.setText(noOfNewVideos-tempCount);
//                holder.NewVideoCount.setVisibility(View.VISIBLE);
//            } else if (videoCount>totalVideos) {
//
//                noOfNewVideos=noOfNewVideos+(videoCount-totalVideos);
//                holder.NewVideoCount.setText(noOfNewVideos);
//                holder.NewVideoCount.setVisibility(View.VISIBLE);
//
//
//
//            }
//        }

    }
    @Override
    public int getItemCount() {
        return videoFolders.size();
    }
}
