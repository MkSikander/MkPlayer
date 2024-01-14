package com.mbytes.mkplayer.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import java.io.File;
import java.util.Comparator;

public class VideoUtils  {



    public interface AdapterCallback {
        void onAdapterMethodCalled();
    }
    private static AdapterCallback adapterCallback;

    public static void setAdapterCallback(AdapterCallback callback) {
        adapterCallback = callback;
    }

    public static String timeConversion(Long millie ) {

        if (millie != null) {
            long seconds = (millie / 1000);
            long sec = seconds % 60;
            long min = (seconds / 60) % 60;
            long hrs = (seconds / (60 * 60)) % 24;
            if (hrs > 0) {
                return String.format("%02d:%02d:%02d", hrs, min, sec);
            } else {
                return String.format("%02d:%02d", min, sec);
            }
        } else {
            return null;

        }
    }

   //Showing Menu
    public static void showMenu(Context context, View anchorView, VideoItem videoItem) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.video_item_menu, null);

        TextView deleteTextView = bottomSheetView.findViewById(R.id.layout_delete);
        deleteTextView.setOnClickListener(v -> {
            MaterialAlertDialogBuilder dialogBuilder= new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_App_MaterialAlertDialog);
            dialogBuilder.setTitle("Are You Sure");
            dialogBuilder.setMessage("Deleted video will not be restored");
            dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteVideo(context,videoItem);
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bottomSheetDialog.dismiss();
                }
            });
            dialogBuilder.show();

            bottomSheetDialog.dismiss();
        });

        TextView shareTextView = bottomSheetView.findViewById(R.id.layout_share);
        shareTextView.setOnClickListener(v -> {
            shareVideo(context,videoItem);
            bottomSheetDialog.dismiss();

        });

        TextView propertiesTextView = bottomSheetView.findViewById(R.id.layout_info);
        propertiesTextView.setOnClickListener(v -> {
            showProperties(videoItem);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
        });
        bottomSheetDialog.show();
    }
    // Video Delete
    private static void deleteVideo(Context context,VideoItem videoItem) {

        String videoPath = videoItem.getVideoPath();
        File videoFile = new File(videoPath);
        if (videoFile.exists()) {
            if (videoFile.delete()) {
                if (adapterCallback != null) {
                    adapterCallback.onAdapterMethodCalled();
                }
                // File deleted successfully
                // You may want to update your UI or perform other actions here
                Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show();

            } else {
                // Unable to delete file
                Toast.makeText(context, "Failed to delete video", Toast.LENGTH_SHORT).show();
            }
        } else {
            // File doesn't exist
            Toast.makeText(context, "Video file not found", Toast.LENGTH_SHORT).show();
        }

    }
    //Video Sharing
    private static void shareVideo(Context context,VideoItem videoItem) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // Set the MIME type for the content you want to share
        shareIntent.setType("video/");
        // Add the video URI to the Intent
        Uri videoUri = Uri.parse(videoItem.getVideoPath());
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
        // Add a subject (optional)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Video");
        // Start the Intent with a chooser
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share Video");
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add this line if needed
        try {
            context.startActivity(chooserIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "No app available to handle the action", Toast.LENGTH_SHORT).show();
        }
    }
    // Video Details
    private static void showProperties(VideoItem videoItem) {
        // Implement logic to show video properties
        // This could include displaying details like resolution, size, etc.
    }


}
