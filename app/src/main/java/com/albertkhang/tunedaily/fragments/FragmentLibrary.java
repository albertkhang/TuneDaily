package com.albertkhang.tunedaily.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.PlaylistActivity;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.UpdateThemeEvent;
import com.albertkhang.tunedaily.views.RoundImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FragmentLibrary extends Fragment {

    private SettingManager settingManager;
    private ConstraintLayout liked_songs_frame;
    private LinearLayout root_view;
    private RelativeLayout top_frame;
    private TextView txtLibrary;
    private TextView txtLikedSongs;
    private TextView txtSongsAmount;
    private ImageView imgMusicNote;
    private RoundImageView playlist_background_frame;
    private TextView txtPlaylist;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());

        liked_songs_frame = view.findViewById(R.id.liked_songs_frame);
        root_view = view.findViewById(R.id.root_view);
        top_frame = view.findViewById(R.id.top_frame);
        txtLibrary = view.findViewById(R.id.txtLibrary);

        txtLikedSongs = view.findViewById(R.id.txtLikedSongs);
        txtSongsAmount = view.findViewById(R.id.txtSongsAmount);
        imgMusicNote = view.findViewById(R.id.imgMusicNote);

        playlist_background_frame = view.findViewById(R.id.playlist_background_frame);
        txtPlaylist = view.findViewById(R.id.txtPlaylist);

        updateTheme();
    }

    private void addEvent() {
        liked_songs_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PlaylistActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));

            txtLibrary.setTextColor(getResources().getColor(R.color.colorLight1));

            txtLikedSongs.setTextColor(getResources().getColor(R.color.colorLight1));
            txtSongsAmount.setTextColor(getResources().getColor(R.color.colorLight3));

            imgMusicNote.setColorFilter(getResources().getColor(R.color.colorLight3));

            playlist_background_frame.setColorFilter(getResources().getColor(R.color.colorDark3));

            txtPlaylist.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));

            txtLibrary.setTextColor(getResources().getColor(R.color.colorDark1));

            txtLikedSongs.setTextColor(getResources().getColor(R.color.colorDark1));
            txtSongsAmount.setTextColor(getResources().getColor(R.color.colorDark3));

            imgMusicNote.setColorFilter(getResources().getColor(R.color.colorDark3));
            playlist_background_frame.setColorFilter(getResources().getColor(R.color.colorLight3));

            txtPlaylist.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateThemeEvent(UpdateThemeEvent updateThemeEvent) {
        updateTheme();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
