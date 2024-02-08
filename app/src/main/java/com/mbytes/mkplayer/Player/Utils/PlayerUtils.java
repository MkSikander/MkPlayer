package com.mbytes.mkplayer.Player.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;
import com.mbytes.mkplayer.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayerUtils {
    private static final float[] playbackSpeeds = {0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2.0f};
    private static int selectedSpeedIndex = 3; //
    private static BottomSheetDialog bottomSheetDialog;
    //getting video Orientation
    public static int getVideoRotation(String videoPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            // Swap width and height for portrait videos
            int width = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
            int height = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            if (width > height) {
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            }
            return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
    public static String convertVideoListToJson(ArrayList<VideoItem> videoList) {
        Gson gson = new Gson();
        return gson.toJson(videoList);
    }

    @OptIn(markerClass = UnstableApi.class)
    public static void setAudioTrack(Player player , DefaultTrackSelector trackSelector, Context context){
        ArrayList<String> audioTrack = new ArrayList<>();
        ArrayList<String> audioList = new ArrayList<>();
        int selectedTrackIndex=-1;
        int a=0;
        Tracks tracks=player.getCurrentTracks();
        for (Tracks.Group trackGroup : tracks.getGroups()) {
            // Group level information.
            boolean trackInGroupIsSupported = trackGroup.isSupported();
            if(trackGroup.getType()== C.TRACK_TYPE_AUDIO && trackInGroupIsSupported) {
                for (int m = 0; m < trackGroup.length; m++ ) {
                    // Individual track information.
                    audioTrack.add(trackGroup.getTrackFormat(m).language.toString());
                    audioList.add(PlayerUtils.getName(trackGroup.getMediaTrackGroup(),m));
                    boolean isSelected = trackGroup.isTrackSelected(m);
                    a++;
                    if (isSelected){
                        selectedTrackIndex=a-1;
                    }
                }
            }
        }

        // If no track is selected, use the size of the list
        if (selectedTrackIndex==-1)
        {
            selectedTrackIndex=0;
        }
        if (audioList.get(0).contains("null")) {
            audioList.set(0, "Default Track");
        }
        boolean isAudioDisabled=trackSelector.getParameters().getRendererDisabled(C.TRACK_TYPE_AUDIO);
        if(isAudioDisabled)
        {
            selectedTrackIndex =audioList.size();
        }
        audioList.add("Disable Audio");
        CharSequence[] tempTracks = audioList.toArray(new CharSequence[audioList.size()]);
        MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle("Select Audio Track");
        dialogBuilder.setOnCancelListener(dialogInterface -> player.play());
        dialogBuilder.setSingleChoiceItems(tempTracks,selectedTrackIndex, (dialogInterface, i) -> {
            if (i!=audioList.size()-1) {
                trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_AUDIO, false));
                trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTrack.get(i)));
            }
            else {
                trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_AUDIO,true));
            }
            player.play();
            dialogInterface.dismiss();
        });
        dialogBuilder.show();
    }
    public static void setPlaybackSpeed(Player player,Context context){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Select Playback Speed");

        builder.setSingleChoiceItems(getPlaybackSpeedLabels(), selectedSpeedIndex, (dialog, which) -> {
            selectedSpeedIndex = which;
            float selectedSpeed = playbackSpeeds[selectedSpeedIndex];
            Toast.makeText(context, "Selected speed: " + selectedSpeed, Toast.LENGTH_SHORT).show();
            player.setPlaybackSpeed(selectedSpeed);
            player.play();
            dialog.dismiss();
        });
        builder.setOnCancelListener(dialogInterface -> player.play());
        builder.show();
    }


    // Method to get labels for playback speeds
    private static CharSequence[] getPlaybackSpeedLabels() {
        CharSequence[] labels = new CharSequence[playbackSpeeds.length];
        for (int i = 0; i < playbackSpeeds.length; i++) {
            labels[i] = String.valueOf(playbackSpeeds[i]+" x");
        }
        return labels;
    }

    public static void showPlaylistVideos(ArrayList<VideoItem> videos,int position,Context context){
         bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.playlist_bottom_sheet);
        TextView bottomSheetTitle = bottomSheetDialog.findViewById(R.id.bottom_sheet_title);
        bottomSheetTitle.setText("Playlist");
        RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.playlist_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        PlaylistVideoAdapter adapter = new PlaylistVideoAdapter(context,videos, position);
        recyclerView.setAdapter(adapter);

        bottomSheetDialog.show();
    }
    public static void hideBottomSheet(){
        bottomSheetDialog.dismiss();
    }


    @OptIn(markerClass = UnstableApi.class)
    public static void setSubTrack(Player player , DefaultTrackSelector trackSelector, Context context){
        ArrayList<String> subTrack = new ArrayList<>();
        ArrayList<String> subList = new ArrayList<>();
        int selectedTrackIndex=-1;
        int a=0;
        Tracks tracks=player.getCurrentTracks();
        for (Tracks.Group trackGroup : tracks.getGroups()) {
            // Group level information.
            boolean trackInGroupIsSupported = trackGroup.isSupported();
            if(trackGroup.getType()== C.TRACK_TYPE_TEXT && trackInGroupIsSupported) {
                for (int m = 0; m < trackGroup.length; m++ ) {
                    // Individual track information.
                    subTrack.add(trackGroup.getTrackFormat(m).language.toString());
                    subList.add(PlayerUtils.getSubName(trackGroup.getMediaTrackGroup(),m));
                    boolean isSelected = trackGroup.isTrackSelected(m);
                    a++;
                    if (isSelected){
                        selectedTrackIndex=a-1;
                    }
                }
            }
        }

        // If no track is selected, use the size of the list
        if (selectedTrackIndex==-1)
        {
            selectedTrackIndex=subList.size();
        }
        boolean isAudioDisabled=trackSelector.getParameters().getRendererDisabled(C.TRACK_TYPE_VIDEO);
        if(isAudioDisabled)
        {
            selectedTrackIndex =subList.size();
        }
        subList.add("Disable Subtitle");
        CharSequence[] tempTracks = subList.toArray(new CharSequence[subList.size()]);
        MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(context);
        dialogBuilder.setTitle("Select Subtitle Track");
        dialogBuilder.setOnCancelListener(dialogInterface -> player.play());
        dialogBuilder.setSingleChoiceItems(tempTracks,selectedTrackIndex, (dialogInterface, i) -> {
            if (i!=subList.size()-1) {
                trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_VIDEO, false));
                trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage(subTrack.get(i)));
            }
            else {
                trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_VIDEO,true));
            }
            player.play();
            dialogInterface.dismiss();
        });
        dialogBuilder.show();
    }


    @OptIn(markerClass = UnstableApi.class)
    public static String getName(TrackGroup trackGroup, int index) {
        Format format = trackGroup.getFormat(0);
        String language = format.language;
        String label = format.label;

        StringBuilder builder = new StringBuilder();
        if (label != null) {
            builder.append(label);
        }
        if (builder.length() == 0) {
            builder.append("Audio Track #").append(index + 1);
        }
        if (language != null && !language.equals("und")) {
            builder.append(" - ");
            builder.append(Locale.forLanguageTag(language).getDisplayLanguage());
        }
        return builder.toString();
    }

    @OptIn(markerClass = UnstableApi.class)
    public static String getSubName(TrackGroup trackGroup, int index) {
        Format format = trackGroup.getFormat(0);
        String language = format.language;
        String label = format.label;

        StringBuilder builder = new StringBuilder();
        if (label != null) {
            builder.append(label);
        }
        if (builder.length() == 0) {
            builder.append("Subtitle #").append(index + 1);
        }
        if (language != null && !language.equals("und")) {
            builder.append(" - ");
            builder.append(Locale.forLanguageTag(language).getDisplayLanguage());
        }
        return builder.toString();
    }

    public static String formatDurationMillisSign(long millis) {
        if (millis >= 0) {
            return "+" + formatDurationMillis(millis);
        } else {
            return "-" + formatDurationMillis(Math.abs(millis));
        }
    }
    @SuppressLint("DefaultLocale")
    public static String formatDurationMillis(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes) -
                TimeUnit.HOURS.toSeconds(hours);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static float pxToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }
}
