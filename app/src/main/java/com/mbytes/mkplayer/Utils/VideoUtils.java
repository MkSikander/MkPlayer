package com.mbytes.mkplayer.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import java.io.File;
import java.util.Date;


public class VideoUtils {

    static AlertDialog alertDialog;

    public interface AdapterCallback {
        void onAdapterMethodCalled();
    }

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
    public static void showMenu(Context context, VideoItem videoItem) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.video_item_menu, null);

        TextView deleteTextView = bottomSheetView.findViewById(R.id.layout_delete);
        deleteTextView.setOnClickListener(v -> {
            MaterialAlertDialogBuilder dialogBuilder = getMaterialAlertDialogBuilder(context, videoItem);
            dialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> bottomSheetDialog.dismiss());
            dialogBuilder.show();

            bottomSheetDialog.dismiss();
        });

        TextView shareTextView = bottomSheetView.findViewById(R.id.layout_share);
        shareTextView.setOnClickListener(v -> {
            shareVideo(context, videoItem);
            bottomSheetDialog.dismiss();

        });

        TextView propertiesTextView = bottomSheetView.findViewById(R.id.layout_info);
        propertiesTextView.setOnClickListener(v -> {
            showProperties(context,videoItem);
            bottomSheetDialog.dismiss();
        });
        TextView renameTextView = bottomSheetView.findViewById(R.id.layout_rename);
        renameTextView.setOnClickListener(v -> {
            renameVideo(context, videoItem);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
        });
        bottomSheetDialog.show();
    }

    @NonNull
    private static MaterialAlertDialogBuilder getMaterialAlertDialogBuilder(Context context, VideoItem videoItem) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle("Are You Sure");
        dialogBuilder.setMessage("Deleted video will not be restored");
        dialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> deleteVideo(context, videoItem));
        return dialogBuilder;
    }

    private static void renameVideo(Context context, VideoItem videoItem) {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
        View customView = LayoutInflater.from(context).inflate(R.layout.custom_rename_dialog, null);
        alertDialogBuilder.setView(customView);

        // Access views in your custom layout
        TextView titleTextView = customView.findViewById(R.id.dialog_title);
        EditText newNameEditText = customView.findViewById(R.id.new_name_edittext);
        Button renameButton = customView.findViewById(R.id.rename_button);
        Button cancelButton=customView.findViewById(R.id.cancel_button);

        cancelButton.setOnClickListener(view -> alertDialog.dismiss());

        // Customize the dialog title if needed
        titleTextView.setText(R.string.rename_video);


        renameButton.setOnClickListener(view -> {
            String newName = newNameEditText.getText().toString().trim();

            if (!newName.isEmpty()) {
                // Get the current video file path
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
            } else {
                Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show();
            }

        });


        alertDialog = alertDialogBuilder.create();
        // Create and show the dialog
        alertDialog.show();
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
        File video=new File(videoItem.getVideoPath());
        // Set the MIME type for the content you want to share
        shareIntent.setType("video/");
        // Add the video URI to the Intent
        Uri videoUri = FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".provider",video);
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
//        TextView audioCodecTextView = customView.findViewById(R.id.info_audio_codec);
//        Button okayButton=customView.findViewById(R.id.ok_btn);


        // Customize the dialog title
        titleTextView.setText("Details");

        // Set video information
        nameTextView.setText("Video Name: " + videoItem.getVideoName());
        sizeTextView.setText("Size: " + convertFileSize(videoItem.getVideoSize()));
        pathTextView.setText("Path: " + videoItem.getVideoPath());
        durationTextView.setText("Duration: " + formatDuration(videoItem));
        dateAddedTextView.setText("Date Added: " + formatDate(videoItem.getDateAdded()));
        formatTextView.setText("Format: " + videoItem.getVideoType());
        resolutionTextView.setText("Resolution: " + videoItem.getVideoResolution());
//        audioCodecTextView.setText("Audio Codec: " + videoItem.getAudioCodec());


        alertDialogBuilder.setPositiveButton("Okay", (dialogInterface, i) -> {

        });
        // Create and show the dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }






// Helper method to format duration (in milliseconds) to HH:mm:ss format
        private static String formatDuration(VideoItem videoItem) {
            double milliSeconds = Double.parseDouble(videoItem.getVideoDuration());
            return timeConversion((long)milliSeconds);
        }

// Helper method to format date (in milliseconds) to a readable date string
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



