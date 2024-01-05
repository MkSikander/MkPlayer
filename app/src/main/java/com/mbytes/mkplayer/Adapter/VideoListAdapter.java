package com.mbytes.mkplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;

import java.io.File;
import java.util.List;


public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    private final List<VideoItem> videos;

    public VideoListAdapter(List<VideoItem> videos) {
        this.videos = videos;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define views in the ViewHolder
        public TextView videoName;
        public ImageView thumbnail;
        Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.video_name);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            context =itemView.getContext();
            // Add other views if needed
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the custom layout
        View view = inflater.inflate(R.layout.item_video, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        VideoItem videoItem = videos.get(position);
        Glide.with(holder.context).load(new File(videoItem.getVideoPath())).override(80,50).into(holder.thumbnail);
        // Set item views based on the data model
        holder.videoName.setText(videoItem.getVideoName());
        // Bind other data if needed
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Context context = view.getContext();
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("path",videoItem.getVideoPath());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }
}
