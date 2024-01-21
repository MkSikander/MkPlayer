package com.mbytes.mkplayer.Adapter;


import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;

public class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> {

    private final ArrayList<VideoFolder> videoFolders;
    public VideoFoldersAdapter(ArrayList<VideoFolder> videoFolders) {
        this.videoFolders = videoFolders;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define views in the ViewHolder
        public TextView folderNameTextView,folderVideoCount;
        public ImageView checkImage;

        public ViewHolder(View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folder_name);
            folderVideoCount=itemView.findViewById(R.id.folder_video_count);
            checkImage =itemView.findViewById(R.id.check_mark);
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
        // Return a new holder instance
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        VideoFolder videoFolder = videoFolders.get(position);
        // Set item views based on the data model
        holder.folderNameTextView.setText(videoFolder.getFolderName());
        holder.folderVideoCount.setText(videoFolder.getVideoCount() +" videos");
        // Bind other data if needed
        holder.itemView.setOnClickListener(view -> {
                // Handle item click
                Context context = view.getContext();
                Intent intent = new Intent(context, VideosListActivity.class);
                intent.putExtra("folderPath", videoFolder.getFolderPath());
                intent.putExtra("nameOfFolder", videoFolder.getFolderName());
                context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return videoFolders.size();
    }
}
