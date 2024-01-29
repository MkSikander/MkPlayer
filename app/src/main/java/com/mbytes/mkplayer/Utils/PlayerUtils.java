package com.mbytes.mkplayer.Utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.mbytes.mkplayer.Model.VideoItem;
import com.mbytes.mkplayer.Player.PlayerActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class PlayerUtils {

    //getting video Orientation
    public static int getVideoRotation(String videoPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            // Swap width and height for portrait videos
            int width = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
            int height = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            if (width > height) {
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

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
}
