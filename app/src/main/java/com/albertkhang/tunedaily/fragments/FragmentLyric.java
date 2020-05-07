package com.albertkhang.tunedaily.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.albertkhang.tunedaily.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLyric extends Fragment {

    public FragmentLyric() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lyric, container, false);
    }
}
