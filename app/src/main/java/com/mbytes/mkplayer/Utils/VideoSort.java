package com.mbytes.mkplayer.Utils;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.content.res.AppCompatResources;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import java.util.Comparator;
public class VideoSort {
    static int selectedItem=-1;
    static boolean tagSwitchValue;
    static boolean isTagSwitchAccessed=false;

    public static void showVideoSortOptionsDialog(Context context,VideoSort.OnSortOptionSelectedListener listener) {
        Preferences preferences = new Preferences(context);
        ImageView title,titleRevers,date,dateReverse;
        MaterialSwitch showNewTag;
        boolean isShowNewTag=preferences.isShowNewVideoTag();
        int checkedItemIndex = getCheckedItemIndex(preferences.getVideoSortPref("sortVideo"));
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        View customDialog = LayoutInflater.from(context).inflate(R.layout.video_sort_dialog, null);
        title = customDialog.findViewById(R.id.name_a_to_z);
        titleRevers = customDialog.findViewById(R.id.name_z_to_a);
        date = customDialog.findViewById(R.id.date_new_to_old);
        dateReverse = customDialog.findViewById(R.id.date_old_to_new);
        showNewTag=customDialog.findViewById(R.id.switch_show_new_tag);
        title.setOnClickListener(view -> {
            selectedItem=0;
            setSelectedButton(title,titleRevers,date,dateReverse,selectedItem,context);
        });
        titleRevers.setOnClickListener(view -> {
            selectedItem=1;
            setSelectedButton(title,titleRevers,date,dateReverse,selectedItem,context);
        });
        date.setOnClickListener(view -> {
            selectedItem=2;
            setSelectedButton(title,titleRevers,date,dateReverse,selectedItem,context);
        });
        dateReverse.setOnClickListener(view -> {
            selectedItem=3;
            setSelectedButton(title,titleRevers,date,dateReverse,selectedItem,context);
        });
        showNewTag.setOnCheckedChangeListener((compoundButton, b) -> {
            tagSwitchValue=b;
            isTagSwitchAccessed=true;
        });
        showNewTag.setChecked(isShowNewTag);
        setSelectedButton(title,titleRevers,date,dateReverse,checkedItemIndex,context);
        dialogBuilder.setView(customDialog)
                .setPositiveButton("Ok",(dialog,which)->{
                    // Handle the selected sorting option
                    if (selectedItem!=-1||isTagSwitchAccessed) {
                        if (selectedItem!=1) {
                            switch (selectedItem) {
                                case 0:
                                    preferences.setVideoSortPref("sortVideo", "sortName");
                                    break;
                                case 1:
                                    preferences.setVideoSortPref("sortVideo", "sortNameR");
                                    break;
                                case 2:
                                    preferences.setVideoSortPref("sortVideo", "sortDate");
                                    break;
                                case 3:
                                    preferences.setVideoSortPref("sortVideo", "sortDateR");
                                    break;
                            }
                        }
                        if (isTagSwitchAccessed){
                            preferences.setShowNewVideoTag(tagSwitchValue);
                        }

                        // Notify the listener that the sorting preference has been updated
                        if (listener != null) {
                            listener.onSortOptionSelected();
                        }
                    }
                    // Dismiss the dialog after handling the selection
                    dialog.dismiss();
                }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .setOnCancelListener(DialogInterface::dismiss);
        dialogBuilder.show();
    }

    private static void setSelectedButton(ImageView title, ImageView titleRevers, ImageView date, ImageView dateReverse, int checkedItemIndex, Context context) {
        switch (checkedItemIndex){
            case 0:
            {
                title.setBackground(AppCompatResources.getDrawable(context,R.drawable.selected_background));
                dateReverse.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                date.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                titleRevers.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));

                break;
            }
            case 1:{
                titleRevers.setBackground(AppCompatResources.getDrawable(context,R.drawable.selected_background));
                title.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                dateReverse.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                date.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));

                break;
            }
            case 2:{
                date.setBackground(AppCompatResources.getDrawable(context,R.drawable.selected_background));
                titleRevers.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                title.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                dateReverse.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                break;
            }
            case 3:{
                dateReverse.setBackground(AppCompatResources.getDrawable(context,R.drawable.selected_background));
                titleRevers.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                title.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                date.setBackground(AppCompatResources.getDrawable(context,R.drawable.unselected_background));
                break;
            }
        }
    }

    private static int getCheckedItemIndex(String sortPreference) {
        switch (sortPreference) {
            case "sortName":
                return 0;
            case "sortNameR":
                return 1;
            case "sortDate":
                return 2;
            case "sortDateR":
                return 3;
            default:
                return 0; // No preference or unknown preference
        }
    }

    public interface OnSortOptionSelectedListener {
        void onSortOptionSelected();
    }

    public static class VideoFilesComparator implements Comparator<VideoItem> {
        private final String sortBy;

        public VideoFilesComparator(Context context) {
            Preferences preferences = new Preferences(context);
            if (preferences.contains("sortVideo")) {
                // Set a default sort preference (e.g., "sortName")
                preferences.setVideoSortPref("sortVideo", "sortName");
            }
            sortBy = preferences.getVideoSortPref("sortVideo");
        }

        @Override
        public int compare(VideoItem videoItem1, VideoItem videoItem2) {
            if ("sortDate".equals(sortBy)) {
                return videoItem2.getDateAdded().compareTo(videoItem1.getDateAdded());
            } else if ("sortName".equals(sortBy)) {
                return videoItem1.getVideoName().compareToIgnoreCase(videoItem2.getVideoName());
            } else if ("sortDateR".equals(sortBy)) {
                return videoItem1.getDateAdded().compareTo(videoItem2.getDateAdded());
            } else if ("sortNameR".equals(sortBy)) {
                return videoItem2.getVideoName().compareToIgnoreCase(videoItem1.getVideoName());
            }
            return 0;
        }
    }
}
