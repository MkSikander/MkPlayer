package com.mbytes.mkplayer.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.mbytes.mkplayer.R;


public class AppearanceFragment extends Fragment {
  private View rootView;
  private MaterialSwitch themeSwitch,contrastSwitch,dynamicSwitch;
    public AppearanceFragment() {
        // Required empty public constructor
    }
    public interface FragmentCallback {
        void onFragmentRemoved();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       rootView= inflater.inflate(R.layout.fragment_appearance, container, false);
       initViews();
       return rootView;
    }

    private void initViews() {
        LinearLayout themeLayout=rootView.findViewById(R.id.theme_layout);
        dynamicSwitch=rootView.findViewById(R.id.dynamic_switch);
        themeSwitch=rootView.findViewById(R.id.dark_switch);
        contrastSwitch=rootView.findViewById(R.id.contrast_switch);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((AppearanceFragment.FragmentCallback)requireActivity()).onFragmentRemoved();
    }
}