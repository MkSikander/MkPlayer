package com.mbytes.mkplayer.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


public class VideoUtils {

    static AlertDialog alertDialog;

    public interface AdapterCallback {
        void onAdapterMethodCalled();
    }

    static Preferences preferences;

    private static AdapterCallback adapterCallback;

    public static void setAdapterCallback(AdapterCallback callback) {
        adapterCallback = callback;
    }

    @SuppressLint("DefaultLocale")
    public static String timeConversion(Long millie) {

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
    public static void showMenu(Context context, VideoItem videoItem, ArrayList<VideoItem> videos) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.video_item_menu, null);
        preferences = new Preferences(context);
        TextView deleteTextView = bottomSheetView.findViewById(R.id.layout_delete);
        deleteTextView.setOnClickListener(v -> showMaterialAlertDialogBuilder(context, videoItem, bottomSheetDialog));
        TextView shareTextView = bottomSheetView.findViewById(R.id.layout_share);
        shareTextView.setOnClickListener(v -> {
            shareVideo(context, videoItem);
            bottomSheetDialog.dismiss();
        });
        TextView propertiesTextView = bottomSheetView.findViewById(R.id.layout_info);
        propertiesTextView.setOnClickListener(v -> {
            showProperties(context, videoItem);
            bottomSheetDialog.dismiss();
        });
        TextView renameTextView = bottomSheetView.findViewById(R.id.layout_rename);
        renameTextView.setOnClickListener(v -> {
            renameVideo(context, videoItem,videos);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
        });
        bottomSheetDialog.show();
    }

    private static void showMaterialAlertDialogBuilder(Context context, VideoItem videoItem, BottomSheetDialog bottomSheetDialog) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        View customView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_video, null);
        dialogBuilder.setView(customView);
        TextView videoName = customView.findViewById(R.id.video_name);
        ImageView thumbnail = customView.findViewById(R.id.thumbnail);
        videoName.setText(videoItem.getVideoName());
        Glide.with(context).load(new File(videoItem.getVideoPath())).into(thumbnail);
        dialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> {
            deleteVideo(context, videoItem);
            alertDialog.dismiss();
        });
        dialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> alertDialog.dismiss());
        dialogBuilder.setOnCancelListener(DialogInterface::dismiss);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
        bottomSheetDialog.dismiss();
    }

    private static void renameVideo(Context context, VideoItem videoItem, ArrayList<VideoItem> videos) {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
        View customView = LayoutInflater.from(context).inflate(R.layout.custom_rename_dialog, null);
        alertDialogBuilder.setView(customView);
        // Access views in your custom layout
        TextView titleTextView = customView.findViewById(R.id.dialog_title);
        EditText newNameEditText = customView.findViewById(R.id.new_name_edittext);
        alertDialogBuilder.setNegativeButton("Cancel", ((dialogInterface, i) -> alertDialog.dismiss()));
        // Customize the dialog title if needed
        titleTextView.setText(R.string.rename_video);
        alertDialogBuilder.setPositiveButton("Rename", (dialogInterface, i) -> {
            String newName = newNameEditText.getText().toString().trim();
            if (!newName.isEmpty() && !newName.startsWith(".")) {
                // Get the current video file path
                if (isDuplicateName(newName,videos)){
                    String videoPath = videoItem.getVideoPath();
                    File videoFile = new File(videoPath);
                    // Create a new file with the desired name in the same directory
                    File newVideoFile = new File(videoFile.getParent(), newName + ".mp4");
                    // Rename the file
                    if (videoFile.renameTo(newVideoFile)) {
                        if (adapterCallback != null) {
                            adapterCallback.onAdapterMethodCalled();
                        }
                        Toast.makeText(context, "Video renamed successfully", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(context, "Failed to rename video", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(context, "Choose another name", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog = alertDialogBuilder.create();
        // Create and show the dialog
        alertDialog.show();
    }

    private static boolean isDuplicateName(String newName, ArrayList<VideoItem> videos) {
        for (int i=0;i<videos.size();i++){
            String videoName = videos.get(i).getVideoName().substring(0, videos.get(i).getVideoName().lastIndexOf('.'));
           Log.d("video Name","name"+videoName);
            if (Objects.equals(videoName, newName)){
                return false;
            }
        }
        return true;
    }

    // Video Delete
    private static void deleteVideo(Context context, VideoItem videoItem) {

        String videoPath = videoItem.getVideoPath();
        File videoFile = new File(videoPath);
        if (videoFile.exists()) {
            if (videoFile.delete()) {
                if (adapterCallback != null) {
                    adapterCallback.onAdapterMethodCalled();
                }
                // File deleted successfully
                // You may want to update your UI or perform other actions here
                preferences.updateFolders(true);
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
    private static void shareVideo(Context context, VideoItem videoItem) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File video = new File(videoItem.getVideoPath());
        // Set the MIME type for the content you want to share
        shareIntent.setType("video/");
        // Add the video URI to the Intent
        Uri videoUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", video);
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
        // Add a subject (optional)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Video");
        // Start the Intent with a chooser
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share Video");
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add this line if needed
        try {
            context.startActivity(chooserIntent);
        } catch (Exception e) {
            Toast.makeText(context, "No app available to handle the action", Toast.LENGTH_SHORT).show();
        }
    }

    // Video Details
    @SuppressLint("SetTextI18n")
    private static void showProperties(Context context, VideoItem videoItem) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
        View customView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        alertDialogBuilder.setView(customView);

        // Access views in your custom layout
        TextView titleTextView = customView.findViewById(R.id.info_title);
        TextView nameTextView = customView.findViewById(R.id.info_name);
        TextView sizeTextView = customView.findViewById(R.id.info_size);
        TextView pathTextView = customView.findViewById(R.id.info_path);
        TextView durationTextView = customView.findViewById(R.id.info_duration);
        TextView dateAddedTextView = customView.findViewById(R.id.info_date_added);
        TextView formatTextView = customView.findViewById(R.id.info_format);
        TextView resolutionTextView = customView.findViewById(R.id.info_resolution);
        // Customize the dialog title
        titleTextView.setText("Video Details");

        // Set video information
        nameTextView.setText("Video Name: " + videoItem.getVideoName());
        sizeTextView.setText("Size: " + convertFileSize(videoItem.getVideoSize()));
        pathTextView.setText("Path: " + videoItem.getVideoPath());
        durationTextView.setText("Duration: " + videoItem.getVideoDuration());
        dateAddedTextView.setText("Date Added: " + formatDate(videoItem.getDateAdded()));
        formatTextView.setText("Format: " + videoItem.getVideoType());
        resolutionTextView.setText("Resolution: " + videoItem.getVideoResolution());
        alertDialogBuilder.setPositiveButton("Ok", (dialogInterface, i) -> {
        });
        // Create and show the dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    // Helper method to format date to a readable date string
    private static String formatDate(Date date) {
        return String.valueOf(date);
    }

    @SuppressLint("DefaultLocale")
    public static String convertFileSize(long fileSizeInBytes) {
        // Convert to gigabytes
        double fileSizeInGB = (double) fileSizeInBytes / (1024 * 1024 * 1024);

        // Convert to megabytes
        double fileSizeInMB = (double) fileSizeInBytes / (1024 * 1024);

        // Format the result
        String result;
        if (fileSizeInGB > 1) {
            result = String.format("%.2f GB", fileSizeInGB);
        } else {
            result = String.format("%.2f MB", fileSizeInMB);
        }

        return result;
    }
}



