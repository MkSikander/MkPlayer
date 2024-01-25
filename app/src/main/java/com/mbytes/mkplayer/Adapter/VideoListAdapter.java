package com.mbytes.mkplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;
import com.mbytes.mkplayer.Utils.VideoUtils;
import java.io.File;
import java.util.ArrayList;




public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> implements VideoUtils.AdapterCallback {


    public interface VideoLoadListener {
        void onVideoLoadRequested();
    }
    private VideoLoadListener videoLoadListener;
    public void setVideoLoadListener(VideoLoadListener listener) {
        this.videoLoadListener = listener;
    }
    private final Preferences sharedPreferences;
    private final ArrayList<VideoItem> videos;

    public VideoListAdapter(ArrayList<VideoItem> videos, Preferences sharedPreferences) {
        this.videos = videos;
        this.sharedPreferences = sharedPreferences;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define views in the ViewHolder
        public TextView videoName, videoDuration, newText;
        public ImageView thumbnail, moreMenu;
        Context context;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.video_name);
            checkBox = itemView.findViewById(R.id.checkbox);
            newText = itemView.findViewById(R.id.symbol_new);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            videoDuration = itemView.findViewById(R.id.video_duration);
            moreMenu = itemView.findViewById(R.id.video_menu_more);
            context = itemView.getContext();


            // Add other views if needed
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // Inflate the custom layout
        View view = inflater.inflate(R.layout.item_video, parent, false);
        // Return a new holder instance
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         VideoItem videoItem = videos.get(position);

        holder.itemView.setOnClickListener(view -> onItemViewClicked(view,videoItem,position));
        holder.itemView.setOnLongClickListener(view -> {
            VideoUtils.showMenu(view.getContext(), videoItem);
            return false;
        });
        double milliSeconds = Double.parseDouble(videoItem.getVideoDuration());
        boolean isVideoPlayed = getVideoPlayedStatus(videoItem.getVideoPath());
        if (isVideoPlayed) {
            holder.newText.setVisibility(View.GONE);
        } else {
            holder.newText.setVisibility(View.VISIBLE);
        }
        Glide.with(holder.context).load(new File(videoItem.getVideoPath())).into(holder.thumbnail);
        holder.videoName.setText(videoItem.getVideoName());
        holder.videoDuration.setText(VideoUtils.timeConversion((long) milliSeconds));
        holder.moreMenu.setOnClickListener(view -> VideoUtils.showMenu(view.getContext(), videoItem));
    }

    @OptIn(markerClass = UnstableApi.class)
    private void onItemViewClicked(View view , VideoItem videoItem, int position) {
        Context context = view.getContext();
        setVideoPlayedStatus(videoItem.getVideoPath());
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("position", position);
        Bundle bundle=new Bundle();
        bundle.putParcelableArrayList("videoArrayList",videos);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void onAdapterMethodCalled() {
        if (videoLoadListener != null) {
            videoLoadListener.onVideoLoadRequested();
        }
    }

    private void setVideoPlayedStatus(String videoPath) {
        // Save video playback status to SharedPreferences
        // Use a unique key for each video
        String videoKey = "played_" + videoPath;
       sharedPreferences.setBoolean(videoKey, true);

    }

    private boolean getVideoPlayedStatus(String videoPath) {
        // Retrieve video playback status from SharedPreferences
        // Use a unique key for each video
        String videoKey = "played_" + videoPath;
        return sharedPreferences.getBoolean(videoKey);
    }


}
