package com.mbytes.mkplayer.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;


public class PlayerSettingFragment extends Fragment {

    MaterialSwitch seekSwitch,scrollSwitch,zoomSwitch,resumeSwitch,brightnessSwitch,autoplaySwitch,fastSeekingSwitch;
    LinearLayout seekIncrement,playbackSpeed,defaultOrientation;
    TextView heading;
    Preferences preferences;

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
       View rootView=inflater.inflate(R.layout.fragment_player_setting, container, false);
       initView(rootView);
       onClick();
       return rootView;
    }

    private void onClick() {
        heading.setOnClickListener(view -> {
            getFragmentManager().popBackStack();
        });
        seekSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setSeekGesture(b));
        scrollSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setScrollGesture(b));
        zoomSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setZoomGesture(b));
        resumeSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setResumePref(b));
        brightnessSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setBrightnessPref(b));
        autoplaySwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setAutoPlayPref(b));
        fastSeekingSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setFastSeekPref(b));
    }

    private void initView(View rootView) {
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
        preferences=new Preferences(rootView.getContext());

        //Get Default or Current preference and switch position
        seekSwitch.setChecked(preferences.getSeekGesture());
        zoomSwitch.setChecked(preferences.getZoomGesture());
        scrollSwitch.setChecked(preferences.getScrollGesture());
        resumeSwitch.setChecked(preferences.getResumePref());
        brightnessSwitch.setChecked(preferences.getBrightnessPref());
        autoplaySwitch.setChecked(preferences.getAutoPlayPref());
        fastSeekingSwitch.setChecked(preferences.getFastSeekPref());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((FragmentCallback)requireActivity()).onFragmentRemoved();
    }
}