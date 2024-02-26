package com.mbytes.mkplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mbytes.mkplayer.Activities.VideosListActivity;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.VideoUtils;
import java.io.File;
import java.util.ArrayList;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> implements VideoUtils.AdapterCallback {

    public interface VideoLoadListener {
        void onVideoLoadRequested();
    }
    private VideosListActivity activity;
    private VideoLoadListener videoLoadListener;
    public void setVideoLoadListener(VideoLoadListener listener, VideosListActivity activity) {
        this.videoLoadListener = listener;
        this.activity=activity;

    }

    private final ArrayList<VideoItem> videos;

    public VideoListAdapter(ArrayList<VideoItem> videos) {
        this.videos = videos;

    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define views in the ViewHolder
        public TextView videoName, videoDuration, newText;
        public ImageView thumbnail, moreMenu;
        Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.video_name);
            newText = itemView.findViewById(R.id.symbol_new);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            videoDuration = itemView.findViewById(R.id.video_duration);
            moreMenu = itemView.findViewById(R.id.video_menu_more);
            context = itemView.getContext();
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // Inflate the custom layout
        View view = inflater.inflate(R.layout.video_item, parent, false);
        // Return a new holder instance
        return new ViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         VideoItem videoItem = videos.get(position);
            Glide.with(holder.context)
                    .load(new File(videoItem.getVideoPath()))
                    .override(95,58)
                    .fitCenter()
                    .into(holder.thumbnail);
                holder.videoName.setText(videoItem.getVideoName());
                holder.videoDuration.setText(videoItem.getVideoDuration());
                holder.moreMenu.setOnClickListener(view -> VideoUtils.showMenu(view.getContext(), videoItem));
                holder.itemView.setOnClickListener(view -> onItemViewClicked(view,position));
                holder.itemView.setOnLongClickListener(view -> {
                    VideoUtils.showMenu(view.getContext(), videoItem);
                    return false;
                });
                if (videoItem.getPlayedStatus()) {
                    holder.newText.setVisibility(View.GONE);
                } else {
                    holder.newText.setVisibility(View.VISIBLE);
                }

    }
    @OptIn(markerClass = UnstableApi.class)
    private void onItemViewClicked(View view , int position) {
        Context context = view.getContext();
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("bri",getCurrentBrightness());
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
    public float getCurrentBrightness()  {
        try {
            int systemBri= Settings.System.getInt(activity.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
            return systemBri/255.0f;
        }
        catch (Exception e){
            return -1.0f;
        }
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
