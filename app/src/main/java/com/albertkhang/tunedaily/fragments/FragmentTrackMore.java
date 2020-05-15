package com.albertkhang.tunedaily.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FragmentTrackMore extends BottomSheetDialogFragment {
    private Track track;
    private SettingManager settingManager;
    private RoundImageView imgCover;
    private TextView txtTitle;
    private TextView txtArtist;
    private LinearLayout root_view;
    private ImageView imgLibrary;
    private ImageView imgDownload;
    private ImageView imgBroadcast;
    private TextView txtLibrary;
    private TextView txtDownload;
    private TextView txtBroadcast;

    public FragmentTrackMore(Track track) {
        this.track = track;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_track_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());
        imgCover = view.findViewById(R.id.imgCover);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtArtist = view.findViewById(R.id.txtArtist);
        root_view = view.findViewById(R.id.root_view);
        imgLibrary = view.findViewById(R.id.imgLibrary);
        imgDownload = view.findViewById(R.id.imgDownload);
        imgBroadcast = view.findViewById(R.id.imgBroadcast);
        txtLibrary = view.findViewById(R.id.txtLibrary);
        txtDownload = view.findViewById(R.id.txtDownload);
        txtBroadcast = view.findViewById(R.id.txtBroadcast);

        handleCoverPlaceholderColor(imgCover);
        txtTitle.setText(track.getTitle());
        txtArtist.setText(track.getArtist());

        updateTheme();
    }

    private void updateTheme() {
        SettingManager settingManager = SettingManager.getInstance(getContext());
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight5));

            imgLibrary.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtLibrary.setTextColor(getResources().getColor(R.color.colorLight1));

            imgDownload.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtDownload.setTextColor(getResources().getColor(R.color.colorLight1));

            imgBroadcast.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtBroadcast.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark5));

            imgLibrary.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtLibrary.setTextColor(getResources().getColor(R.color.colorDark1));

            imgDownload.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtDownload.setTextColor(getResources().getColor(R.color.colorDark1));

            imgBroadcast.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtBroadcast.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    private void addEvent() {

    }

    private void handleCoverPlaceholderColor(RoundImageView imgCover) {
        if (settingManager.isDarkTheme()) {
            Glide.with(requireContext()).load(track.getCover()).placeholder(R.color.colorLight5).into(imgCover);
        } else {
            Glide.with(requireContext()).load(track.getCover()).placeholder(R.color.colorDark5).into(imgCover);
        }
    }
}
