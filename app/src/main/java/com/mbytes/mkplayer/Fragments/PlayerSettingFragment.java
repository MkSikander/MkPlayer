package com.mbytes.mkplayer.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mbytes.mkplayer.R;

import java.util.Objects;


public class PlayerSettingFragment extends Fragment {


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

       return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((FragmentCallback)requireActivity()).onFragmentRemoved();
    }
}