package com.mbytes.mkplayer.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mbytes.mkplayer.Model.VideoFolder;
import com.mbytes.mkplayer.R;

import java.util.Comparator;

public class FolderSort {

    private static final String MY_PREF = "my_pref";

    public static void showSortOptionsDialog(Context context, OnSortOptionSelectedListener listener) {
        SharedPreferences preferences = context.getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        final String[] items = {"Name (A-Z)", "Name (Z - A)", "Date (New - Old)", "Date (Old - New)"};
        int checkedItemIndex = getCheckedItemIndex(preferences.getString("sort", "abc"));

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle("Sort Options")
                .setSingleChoiceItems(new CustomSortAdapter(context, items, checkedItemIndex), checkedItemIndex, (dialog, which) -> {
                    // Handle the selected sorting option
                    switch (which) {
                        case 0:
                            editor.putString("sort", "sortName");
                            break;
                        case 1:
                            editor.putString("sort", "sortNamer");
                            break;
                        case 2:
                            editor.putString("sort", "sortDate");
                            break;
                        case 3:
                            editor.putString("sort", "sortDater");
                            break;
                    }
                    editor.apply();

                    // Notify the listener that the sorting preference has been updated
                    if (listener != null) {
                        listener.onSortOptionSelected();
                    }

                    // Dismiss the dialog after handling the selection
                    dialog.dismiss();
                });


        dialogBuilder.show();
    }

    private static class CustomSortAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final int checkedItemIndex;

        public CustomSortAdapter(Context context, String[] items, int checkedItemIndex) {
            super(context, R.layout.custom_sort_item, items);
            this.context = context;
            this.checkedItemIndex = checkedItemIndex;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.custom_sort_item, parent, false);
                int themeColor = getThemeColor(context,R.color.colorLightDark);
                convertView.setBackgroundColor(themeColor);
            }

            TextView textView = convertView.findViewById(R.id.text);
            ImageView iconView = convertView.findViewById(R.id.icon);

            String item = getItem(position);
            textView.setText(item);

            // Set the icon based on position (replace with your own logic)
            int iconResId = getIconResId(position);
            iconView.setImageResource(iconResId);

            // Highlight the selected item
            if (position == checkedItemIndex) {
                convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
                textView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
                iconView.setColorFilter(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
        }

        private int getIconResId(int position) {
            // Replace with your logic to get the icon resource ID based on position
            switch (position) {
                case 1:
                    return R.drawable.sort_name_reverse;
                case 2:
                    return R.drawable.sort_date;
                case 3:
                    return R.drawable.sort_date_reverse;
                default:
                    return R.drawable.sort_name;
            }
        }
    }

    private static int getCheckedItemIndex(String sortPreference) {
        switch (sortPreference) {
            case "sortName":
                return 0;
            case "sortNamer":
                return 1;
            case "sortDate":
                return 2;
            case "sortDater":
                return 3;
            default:
                return -1; // No preference or unknown preference
        }
    }

    public interface OnSortOptionSelectedListener {
        void onSortOptionSelected();
    }

    private static int getThemeColor(Context context, int attribute) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attribute, typedValue, true);
        return typedValue.data;
    }



        public static class VideoFolderComparator implements Comparator<VideoFolder> {
            private final String sortBy;

            public VideoFolderComparator(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
                if (!preferences.contains("sort")) {
                    // Set a default sort preference (e.g., "sortName")
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("sort", "sortName");
                    editor.apply();
                }
                sortBy = preferences.getString("sort", "sortName");
            }

            @Override
            public int compare(VideoFolder folder1, VideoFolder folder2) {
                if ("sortDate".equals(sortBy)) {
                    return folder2.getDateAdded().compareTo(folder1.getDateAdded());
                } else if ("sortName".equals(sortBy)) {
                    return folder1.getFolderName().compareToIgnoreCase(folder2.getFolderName());
                } else if ("sortDater".equals(sortBy)) {
                    return folder1.getDateAdded().compareTo(folder2.getDateAdded());
                } else if ("sortNamer".equals(sortBy)) {
                    return folder2.getFolderName().compareToIgnoreCase(folder1.getFolderName());
                }

                return 0;
            }
        }
    }

