package com.albertkhang.tunedaily.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.Playlist;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PlaylistMoreFragment extends BottomSheetDialogFragment {
    private Playlist playlist;
    private RoundImageView imgCover;
    private TextView txtTitle;

    public PlaylistMoreFragment(Playlist playlist) {
        this.playlist = new Playlist(playlist);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        updateDataIntent();
        addEvent();
    }

    private void updateDataIntent() {
        txtTitle.setText(playlist.getName());
        Glide.with(this).load(playlist.getCover()).placeholder(R.drawable.ic_favourite_red).into(imgCover);
    }

    private void addControl(View view) {
        imgCover = view.findViewById(R.id.imgCover);
        txtTitle = view.findViewById(R.id.txtTitle);
    }

    private void addEvent() {

    }
}
