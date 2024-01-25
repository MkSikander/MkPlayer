package com.mbytes.mkplayer.Utils;
import android.content.Context;
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
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.R;
import java.util.Comparator;
public class VideoSort {


    public static void showVideoSortOptionsDialog(Context context,VideoSort.OnSortOptionSelectedListener listener) {
        Preferences preferences = new Preferences(context);

        final String[] items = {"Name (A-Z)", "Name (Z - A)", "Date (New - Old)", "Date (Old - New)"};
        int checkedItemIndex = getCheckedItemIndex(preferences.getVideoSortPref("sortVideo"));

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle("Sort Options")
                .setSingleChoiceItems(new VideoSort.CustomSortAdapter(context, items, checkedItemIndex), checkedItemIndex, (dialog, which) -> {
                    // Handle the selected sorting option
                    switch (which) {
                        case 0:
                           preferences.setVideoSortPref("sortVideo", "sortName");
                            break;
                        case 1:
                            preferences.setVideoSortPref("sortVideo", "sortNamer");
                            break;
                        case 2:
                            preferences.setVideoSortPref("sortVideo", "sortDate");
                            break;
                        case 3:
                            preferences.setVideoSortPref("sortVideo", "sortDater");
                            break;
                    }


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
                return 0; // No preference or unknown preference
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
            } else if ("sortDater".equals(sortBy)) {
                return videoItem1.getDateAdded().compareTo(videoItem2.getDateAdded());
            } else if ("sortNamer".equals(sortBy)) {
                return videoItem2.getVideoName().compareToIgnoreCase(videoItem1.getVideoName());
            }

            return 0;
        }
    }

}
