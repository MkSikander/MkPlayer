package com.mbytes.mkplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.VideoUtils;
import java.io.File;
import java.util.List;


public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> implements VideoUtils.AdapterCallback{



    public interface VideoLoadListener {
        void onVideoLoadRequested();
    }
    private VideoLoadListener videoLoadListener;

    public void setVideoLoadListener(VideoLoadListener listener) {
        this.videoLoadListener = listener;
    }
    private final SharedPreferences sharedPreferences;


    private final List<VideoItem> videos;
    public VideoListAdapter(List<VideoItem> videos, SharedPreferences sharedPreferences) {
        this.videos = videos;
        this.sharedPreferences=sharedPreferences;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define views in the ViewHolder
        public TextView videoName,videoDuration,newText;
        public ImageView thumbnail, moreMenu;
        Context context;
        public ViewHolder(View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.video_name);
            newText = itemView.findViewById(R.id.symbol_new);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            videoDuration =itemView.findViewById(R.id.video_duration);
            moreMenu = itemView.findViewById(R.id.video_menu_more);
            context =itemView.getContext();
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
        // Get the data model based on position
        VideoItem videoItem = videos.get(position);
        // Set symbol based on video playback status

        double milliSeconds = Double.parseDouble(videoItem.getVideoDuration());

        boolean isVideoPlayed = getVideoPlayedStatus(videoItem.getVideoPath());
        if (isVideoPlayed) {
            holder.newText.setVisibility(View.GONE);
        } else {
            holder.newText.setVisibility(View.VISIBLE);
        }

        Glide.with(holder.context).load(new File(videoItem.getVideoPath())).into(holder.thumbnail);

        holder.videoName.setText(videoItem.getVideoName());
        holder.videoDuration.setText(VideoUtils.timeConversion((long)milliSeconds));

        // Bind other data if needed
        holder.itemView.setOnClickListener(view -> {
          Context context = view.getContext();
            setVideoPlayedStatus(videoItem.getVideoPath());
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("path",videoItem.getVideoPath());
            context.startActivity(intent);
        });
        
        holder.moreMenu.setOnClickListener(view -> VideoUtils.showMenu(view.getContext(), videoItem));
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(videoKey, true);
        editor.apply();
    }
    private boolean getVideoPlayedStatus(String videoPath) {
        // Retrieve video playback status from SharedPreferences
        // Use a unique key for each video
        String videoKey = "played_" + videoPath;
        return sharedPreferences.getBoolean(videoKey, false);
    }


}
