package com.mbytes.mkplayer.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.mbytes.mkplayer.Activities.SettingsActivity;
import com.mbytes.mkplayer.Player.Utils.PlayerUtils;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.FolderSort;
import com.mbytes.mkplayer.Utils.Preferences;
import java.util.Locale;


public class PlayerSettingFragment extends Fragment {

    MaterialSwitch seekSwitch,scrollSwitch,zoomSwitch,resumeSwitch,brightnessSwitch,autoplaySwitch,fastSeekingSwitch;
    LinearLayout seekIncrement,playbackSpeed,defaultOrientation;
    TextView heading,SeekTime,orientText;
    View rootView;
    Preferences preferences;
    private final String[] orientations = {"Portrait", "Landscape", "Video Orientation"};
    private static final Long[] seekIncrementTime={10000L,15000L,20000L,25000L,30000L,35000L,40000L,45000L,50000L};
    private static final float[] playbackSpeeds = {0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2.0f};
    private static int selectedSpeedIndex, selectedOrient;
    private static int selectedSeekSpeedIndex;

    public PlayerSettingFragment() {
        // Required empty public constructor
    }
    public interface FragmentCallback {
        void onFragmentRemoved();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       rootView=inflater.inflate(R.layout.fragment_player_setting, container, false);
       initView();
       setSeekTime();
       setOrientText(selectedOrient);
       onClick();
       return rootView;
    }

    private void initView() {
        heading=rootView.findViewById(R.id.heading_player_setting);
        playbackSpeed=rootView.findViewById(R.id.playback_speed_layout);
        seekIncrement=rootView.findViewById(R.id.seek_increment_layout);
        defaultOrientation=rootView.findViewById(R.id.orientation_layout);
        seekSwitch=rootView.findViewById(R.id.seek_gesture_setting);
        scrollSwitch=rootView.findViewById(R.id.scroll_gesture_setting);
        zoomSwitch=rootView.findViewById(R.id.zoom_gesture_setting);
        resumeSwitch=rootView.findViewById(R.id.resume_playback_setting);
        brightnessSwitch=rootView.findViewById(R.id.remember_brightness_setting);
        autoplaySwitch=rootView.findViewById(R.id.autoplay_setting);
        fastSeekingSwitch=rootView.findViewById(R.id.fast_seek_setting);
        orientText=rootView.findViewById(R.id.orientation_text);
        preferences=new Preferences(rootView.getContext());
        SeekTime=rootView.findViewById(R.id.seek_time);
        //Get Default or Current preference and switch position
        selectedOrient=preferences.getDefaultOrientation();
        seekSwitch.setChecked(preferences.getSeekGesture());
        zoomSwitch.setChecked(preferences.getZoomGesture());
        scrollSwitch.setChecked(preferences.getScrollGesture());
        resumeSwitch.setChecked(preferences.getResumePref());
        brightnessSwitch.setChecked(preferences.getBrightnessPref());
        autoplaySwitch.setChecked(preferences.getAutoPlayPref());
        fastSeekingSwitch.setChecked(preferences.getFastSeekPref());

    }
    private void showOrientationDialog() {
        int selectedOrientation=preferences.getDefaultOrientation();
        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(rootView.getContext());
        builder.setTitle("Choose Default Orientation")
                .setSingleChoiceItems(orientations, selectedOrientation, (dialogInterface, i) ->{
                    preferences.setDefaultOrientation(i);
                    setOrientText(i);
                    dialogInterface.dismiss();
                });
        builder.show();
    }
    private void onClick() {
        heading.setOnClickListener(view -> {
            assert getFragmentManager() != null;
            getFragmentManager().popBackStack();
        });
        selectedSpeedIndex=preferences.getDefaultPlaybackSpeed();
        seekSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setSeekGesture(b));
        scrollSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setScrollGesture(b));
        zoomSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setZoomGesture(b));
        resumeSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setResumePref(b));
        brightnessSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setBrightnessPref(b));
        autoplaySwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setAutoPlayPref(b));
        fastSeekingSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setFastSeekPref(b));
        playbackSpeed.setOnClickListener(view -> showPlaybackSpeedDialog());
        seekIncrement.setOnClickListener(view -> showSeekIncrementDialog());
        defaultOrientation.setOnClickListener(view -> {
            showOrientationDialog();
        });

    }
    private void showSeekIncrementDialog() {

        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(rootView.getContext());
        LayoutInflater inflater = LayoutInflater.from(rootView.getContext());
        View dialogView = inflater.inflate(R.layout.dialog_seek_increment, null);
        builder.setView(dialogView);
        Slider slider = dialogView.findViewById(R.id.slider_playback_speed);
        TextView speedText=dialogView.findViewById(R.id.speed_text);
        slider.setValue(selectedSeekSpeedIndex); // Set initial value
        slider.setStepSize(1);
        Long Speed = seekIncrementTime[selectedSeekSpeedIndex];
        speedText.setText(PlayerUtils.formatDurationMillis(Speed));
        slider.setLabelFormatter(value -> {
            // Format the label (e.g., "1x")
            long speed = seekIncrementTime[(int) value];
            return PlayerUtils.formatDurationMillis(speed);
        });
        slider.addOnChangeListener((slider1, value, fromUser) -> {
            selectedSeekSpeedIndex = (int) value; // Update selected speed index
            speedText.setText((PlayerUtils.formatDurationMillis(seekIncrementTime[selectedSeekSpeedIndex])));
            preferences.setDefaultSeekSpeed(selectedSeekSpeedIndex);
            setSeekTime();

        });

        // Set listener for cancel action
        builder.setOnCancelListener(dialogInterface -> {});
        // Show the dialog
        builder.show();

    }
    @SuppressLint("SetTextI18n")
    private void showPlaybackSpeedDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(rootView.getContext());
        // Set up the layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(rootView.getContext());
        View dialogView = inflater.inflate(R.layout.dialog_playback_speed, null);
        builder.setView(dialogView);
        // Find the slider in the dialog layout
        Slider slider = dialogView.findViewById(R.id.slider_playback_speed);
        TextView speedText=dialogView.findViewById(R.id.speed_text);
        ImageView increaseImg=dialogView.findViewById(R.id.increase);
        ImageView decreaseImg=dialogView.findViewById(R.id.decrease);
        // Set up the slider
        slider.setValue(selectedSpeedIndex); // Set initial value
        slider.setStepSize(1); // Set step size
        float Speed = playbackSpeeds[selectedSpeedIndex];
        speedText.setText((Speed) + " X");
        slider.setLabelFormatter(value -> {
            // Format the label (e.g., "1x")
            float speed = playbackSpeeds[(int) value];
            return String.format(Locale.getDefault(), "%.1fx", speed);
        });
        increaseImg.setOnClickListener(view1 -> {
            if (selectedSpeedIndex<7) {
                selectedSpeedIndex = selectedSpeedIndex + 1;
                float selectedSpeed = playbackSpeeds[selectedSpeedIndex];
                speedText.setText((selectedSpeed) + " X");
                slider.setValue(selectedSpeedIndex);
                preferences.setDefaultPlaybackSpeed(selectedSpeedIndex);

            }
        });
        decreaseImg.setOnClickListener(view1 -> {
            if (selectedSpeedIndex>0) {
                selectedSpeedIndex = selectedSpeedIndex - 1;
                float selectedSpeed = playbackSpeeds[selectedSpeedIndex];
                slider.setValue(selectedSpeedIndex);
                speedText.setText((selectedSpeed) + " X");
                preferences.setDefaultPlaybackSpeed(selectedSpeedIndex);
            }

        });

        // Set listener for slider value changes
        slider.addOnChangeListener((slider1, value, fromUser) -> {
            selectedSpeedIndex = (int) value; // Update selected speed index
            float selectedSpeed = playbackSpeeds[selectedSpeedIndex]; // Get selected speed
            speedText.setText((selectedSpeed)+"X");
            preferences.setDefaultPlaybackSpeed(selectedSpeedIndex);

        });

        // Set listener for cancel action
        builder.setOnCancelListener(dialogInterface -> {});

        // Show the dialog
        builder.show();


    }
    @SuppressLint("SetTextI18n")
    private void setSeekTime() {
        SeekTime.setText(PlayerUtils.formatDurationMillis(seekIncrementTime[preferences.getDefaultSeekSpeed()])+" Seconds");

    }
    private void setOrientText(int i){
        orientText.setText(orientations[i]);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((FragmentCallback)requireActivity()).onFragmentRemoved();
    }
}