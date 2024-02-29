package com.mbytes.mkplayer.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.content.res.AppCompatResources;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.R;
import java.util.Comparator;

public class FolderSort {
    static int selectedItem=-1;
    static boolean tagSwitchValue;
    static boolean countSwitchValue;
    static boolean isCountSwitchAccessed=false;
    static boolean isTagSwitchAccessed=false;
    public static void showQuickSettingDialog(Context context, OnSortOptionSelectedListener listener) {
        Preferences preferences = new Preferences(context);
        ImageView title,titleRevers,date,dateReverse;
        MaterialSwitch showNewTag,showVideoCount;
        int checkedItemIndex = getCheckedItemIndex(preferences.getFolderSortPref("sort"));
        boolean isShowNewTag=preferences.isShowNewTag();
        boolean isShowVideoCount=preferences.isShowVideoCount();
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        View customDialog = LayoutInflater.from(context).inflate(R.layout.quick_setting_dialog, null);
        title = customDialog.findViewById(R.id.name_a_to_z);
        titleRevers = customDialog.findViewById(R.id.name_z_to_a);
        date = customDialog.findViewById(R.id.date_new_to_old);
        dateReverse = customDialog.findViewById(R.id.date_old_to_new);
        showNewTag=customDialog.findViewById(R.id.switch_show_new_tag);
        showVideoCount=customDialog.findViewById(R.id.switch_show_video_count);
        showNewTag.setChecked(isShowNewTag);
        showVideoCount.setChecked(isShowVideoCount);
        setSelectedButton(title,titleRevers,date,dateReverse,checkedItemIndex,context);
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
        showVideoCount.setOnCheckedChangeListener((compoundButton, b) -> {
            countSwitchValue=b;
            isCountSwitchAccessed=true;
        });
        dialogBuilder.setView(customDialog)
                .setPositiveButton("Done",(dialog,which)->{
                    // Handle the selected sorting option
                    if (selectedItem!=-1 || isCountSwitchAccessed||isTagSwitchAccessed){
                    if (selectedItem!=-1){
                    switch (selectedItem) {
                        case 0:
                            preferences.setFolderSortPref("sort", "sortName");
                            break;
                        case 1:
                            preferences.setFolderSortPref("sort", "sortNameR");
                            break;
                        case 2:
                            preferences.setFolderSortPref("sort", "sortDate");
                            break;
                        case 3:
                            preferences.setFolderSortPref("sort", "sortDateR");
                            break;
                    }}
                    else if (isCountSwitchAccessed){
                        preferences.setShowVidCount(countSwitchValue);
                    }
                    if (isTagSwitchAccessed){
                        preferences.setShowNewTag(tagSwitchValue);
                    }
                        if (listener != null) {
                            listener.onSortOptionSelected();
                        }

                    dialog.dismiss();
                }}
                ).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
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

        }
        return 0;
    }

    public interface OnSortOptionSelectedListener {
        void onSortOptionSelected();
    }



    public static class VideoFolderComparator implements Comparator<VideoFolder> {
        private final String sortBy;

        public VideoFolderComparator(Context context) {
            Preferences preferences = new Preferences(context);
            if (preferences.contains("sort")) {
                // Set a default sort preference (e.g., "sortName")
                preferences.setFolderSortPref("sort", "sortName");
            }
            sortBy = preferences.getFolderSortPref("sort");
        }

        @Override
        public int compare(VideoFolder folder1, VideoFolder folder2) {
            if ("sortDate".equals(sortBy)) {
                return folder2.getDateAdded().compareTo(folder1.getDateAdded());
            } else if ("sortName".equals(sortBy)) {
                return folder1.getFolderName().compareToIgnoreCase(folder2.getFolderName());
            } else if ("sortDateR".equals(sortBy)) {
                return folder1.getDateAdded().compareTo(folder2.getDateAdded());
            } else if ("sortNameR".equals(sortBy)) {
                return folder2.getFolderName().compareToIgnoreCase(folder1.getFolderName());
            }
            return 0;
        }
    }
}

