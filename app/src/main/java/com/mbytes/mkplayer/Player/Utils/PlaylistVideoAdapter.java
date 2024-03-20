package com.mbytes.mkplayer.Player.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;
import java.io.File;
import java.util.ArrayList;

public class PlaylistVideoAdapter extends RecyclerView.Adapter<PlaylistVideoAdapter.ViewHolder> {

    private final ArrayList<VideoItem> mVideos;

    private int mSelectedPosition;
    private final Context mContext;


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

    @SuppressLint("NotifyDataSetChanged")
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VideoItem video = mVideos.get(position);
        holder.videoName.setText(video.getVideoName());
        Glide.with(holder.context).load(new File(video.getVideoPath())).into(holder.videoImage);
        // Highlight the video at the given position
        if (position == mSelectedPosition) {
            holder.itemView.setBackground(AppCompatResources.getDrawable(mContext,R.drawable.chip_video_count));
            holder.videoName.setTextColor(Color.BLACK);
            // Highlight the item
        } else {
            holder.itemView.setBackground(AppCompatResources.getDrawable(mContext,R.drawable.unselected_background));
            holder.videoName.setTextColor(Color.WHITE);// Reset background color
        }

      holder.itemView.setOnClickListener(view -> {
          ((PlayerActivity) mContext).playThis(position);
          mSelectedPosition=position;
          notifyDataSetChanged();
      });
        holder.removeVideoFromList.setOnClickListener(view -> {
            if (position>=0&&position<mVideos.size()){
                if (position==mSelectedPosition){
                    mVideos.remove(position);
                    ((PlayerActivity)mContext).updateList(mVideos);
                    ((PlayerActivity) mContext).playThis(position);
                    notifyDataSetChanged();
                }
                if (position<mSelectedPosition){
                    mSelectedPosition=mSelectedPosition-1;
                    mVideos.remove(position);
                    ((PlayerActivity)mContext).updateList(mVideos);
                    notifyDataSetChanged();
                }
                if (position>mSelectedPosition){
                    mVideos.remove(position);
                    ((PlayerActivity)mContext).updateList(mVideos);
                    notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoImage,removeVideoFromList;
        TextView videoName;
        Context context;
        @UnstableApi

        @OptIn(markerClass = UnstableApi.class)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoImage = itemView.findViewById(R.id.playlist_video_image);
            removeVideoFromList=itemView.findViewById(R.id.remove_vid_from_playlist);
            videoName = itemView.findViewById(R.id.playlist_video_name);
            context= itemView.getContext();

        }
    }
}

