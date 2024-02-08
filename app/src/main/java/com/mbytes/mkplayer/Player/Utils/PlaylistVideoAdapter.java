package com.mbytes.mkplayer.Player.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.io.File;
import java.util.ArrayList;

public class PlaylistVideoAdapter extends RecyclerView.Adapter<PlaylistVideoAdapter.ViewHolder> {

    private ArrayList<VideoItem> mVideos;

    private int mSelectedPosition = -1;
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @OptIn(markerClass = UnstableApi.class)
    public PlaylistVideoAdapter(Context mContext, ArrayList<VideoItem> videos, int mSelectedPosition) {
        this.mContext=mContext;
        this.mVideos = videos;
        this.mSelectedPosition=mSelectedPosition;

    }

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoItem video = mVideos.get(position);
        holder.videoName.setText(video.getVideoName());
        Glide.with(holder.context).load(new File(video.getVideoPath())).into(holder.videoImage);
        // Highlight the video at the given position
        if (position == mSelectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Highlight the item
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Reset background color
        }

      holder.itemView.setOnClickListener(view -> {
          ((PlayerActivity) mContext).playThis(position);

      });
    }


    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoImage;
        TextView videoName;
        Context context;
        @UnstableApi


        @OptIn(markerClass = UnstableApi.class)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoImage = itemView.findViewById(R.id.playlist_video_image);
            videoName = itemView.findViewById(R.id.playlist_video_name);
            context= itemView.getContext();


        }
    }
}

