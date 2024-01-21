package com.mbytes.mkplayer.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class FolderUtils {
    static AlertDialog alertDialog;
    public interface AdapterCallback {
        void onAdapterMethodCalled();
    }

    private static AdapterCallback adapterCallback;

    public static void setAdapterCallback(AdapterCallback callback) {
        adapterCallback = callback;
    }
    public static void showMenu(Context context, VideoFolder videoFolder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.video_item_menu, null);

        TextView deleteTextView = bottomSheetView.findViewById(R.id.layout_delete);
        deleteTextView.setOnClickListener(v -> {
            MaterialAlertDialogBuilder dialogBuilder = getMaterialAlertDialogBuilder(context, videoFolder);
            dialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> bottomSheetDialog.dismiss());
            dialogBuilder.show();

            bottomSheetDialog.dismiss();
        });

        TextView shareTextView = bottomSheetView.findViewById(R.id.layout_share);
        shareTextView.setOnClickListener(v -> {
            shareFolder(context, videoFolder);
            bottomSheetDialog.dismiss();
        });

        TextView propertiesTextView = bottomSheetView.findViewById(R.id.layout_info);
        propertiesTextView.setOnClickListener(v -> {
            showProperties(context,videoFolder);
            bottomSheetDialog.dismiss();
        });
        TextView renameTextView = bottomSheetView.findViewById(R.id.layout_rename);
        renameTextView.setOnClickListener(v -> {
            renameVideo(context, videoFolder);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
        });


        bottomSheetDialog.show();
    }

    @NonNull
    private static MaterialAlertDialogBuilder getMaterialAlertDialogBuilder(Context context, VideoFolder videoFolder) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle("Are You Sure");
        dialogBuilder.setMessage("Deleted video will not be restored");
        dialogBuilder.setPositiveButton("Delete", (dialogInterface, i) -> deleteFolder(videoFolder));

        return dialogBuilder;
    }
//renaming Folder
    private static void renameVideo(Context context, VideoFolder videoFolder) {
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
        titleTextView.setText(R.string.rename_folder);


        renameButton.setOnClickListener(view -> {
            String newName = newNameEditText.getText().toString().trim();
            renameFolder(videoFolder.getFolderPath(), newName);


        });


        alertDialog = alertDialogBuilder.create();
        // Create and show the dialog
        alertDialog.show();
    }
    private static void renameFolder( String currentFolderPath, String newName) {
        File currentFolder = new File(currentFolderPath);
        File parentDirectory = currentFolder.getParentFile();

        if (parentDirectory != null && parentDirectory.exists() && parentDirectory.isDirectory()) {
            File newFolder = new File(parentDirectory, newName);

            if (currentFolder.renameTo(newFolder)) {
                // Folder renamed successfully
                if (adapterCallback != null) {
                    adapterCallback.onAdapterMethodCalled();
                }
                alertDialog.dismiss();
            } else {
                // Failed to rename folder
              alertDialog.dismiss();
            }
        } else {
            // Invalid parent directory
             alertDialog.dismiss();
        }}
    // Folder Delete
    private static void deleteFolder( VideoFolder videoFolder) {
        File folder = new File(videoFolder.getFolderPath());

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (isVideoFile(file)) {
                        if (file.delete()) {
                            // File deleted successfully

                            Log.d("Delete Video", "Deleted: " + file.getAbsolutePath());
                        } else {
                            // Failed to delete file
                            Log.e("Delete Video", "Failed to delete: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (adapterCallback != null) {
                adapterCallback.onAdapterMethodCalled();
            }
        }
    }

    //Video Sharing
    private static void shareFolder(Context context, VideoFolder videoFolder) {
        String folderPath = videoFolder.getFolderPath();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("video/*");

        ArrayList<Uri> videoUris = new ArrayList<>();
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (isVideoFile(file)) {
                        Uri videoUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                        videoUris.add(videoUri);
                    }
                }
            }
        }

        if (!videoUris.isEmpty()) {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, videoUris);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Share videos"));
        } else {
            // Handle case where no video files are found in the folder
            Toast.makeText(context, "No video files found in the folder", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to check if a file is a video file
    private static boolean isVideoFile(File file) {
        String name = file.getName();
        String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".3gp"}; // Add more extensions if needed
        for (String extension : videoExtensions) {
            if (name.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    // Video Details
    @SuppressLint("SetTextI18n")
    private static void showProperties(Context context, VideoFolder videoFolder) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
        View customView = LayoutInflater.from(context).inflate(R.layout.dialog_info_folder, null);
        alertDialogBuilder.setView(customView);

        // Access views in your custom layout
        TextView titleTextView = customView.findViewById(R.id.info_title);
        TextView nameTextView = customView.findViewById(R.id.info_name);
        TextView sizeTextView = customView.findViewById(R.id.info_size);
        TextView pathTextView = customView.findViewById(R.id.info_path);
        TextView dateAddedTextView = customView.findViewById(R.id.info_date_added);

        // Customize the dialog title
        titleTextView.setText("Details");
        // Set video information
        nameTextView.setText("Folder Name: " + videoFolder.getFolderName());
        sizeTextView.setText("Size: " + convertFileSize(videoFolder.getFolderSize()));
        pathTextView.setText("Path: " + videoFolder.getFolderPath());
        dateAddedTextView.setText("Date Added: " + formatDate(videoFolder.getDateAdded()));
        alertDialogBuilder.setPositiveButton("Okay", (dialogInterface, i) -> {

        });
        // Create and show the dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
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
