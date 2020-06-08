package com.albertkhang.tunedaily.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TrackMoreFragment extends BottomSheetDialogFragment {
    private Track track;
    private SettingManager settingManager;
    private RoundImageView imgCover;
    private TextView txtTitle;
    private TextView txtArtist;
    private LinearLayout root_view;
    private ImageView imgLibrary;
    private ImageView imgPlaylist;
    private ImageView imgDownload;
    private ImageView imgBroadcast;
    private TextView txtLibrary;
    private TextView txtPlaylist;
    private TextView txtDownload;
    private TextView txtBroadcast;
    private ConstraintLayout add_to_library_frame;

    public TrackMoreFragment(Track track) {
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
        imgPlaylist = view.findViewById(R.id.imgPlaylist);
        imgDownload = view.findViewById(R.id.imgDownload);
        imgBroadcast = view.findViewById(R.id.imgBroadcast);
        txtLibrary = view.findViewById(R.id.txtLibrary);
        txtPlaylist = view.findViewById(R.id.txtPlaylist);
        txtDownload = view.findViewById(R.id.txtDownload);
        txtBroadcast = view.findViewById(R.id.txtBroadcast);
        add_to_library_frame = view.findViewById(R.id.add_to_library_frame);

        handleCoverPlaceholderColor(imgCover);
        txtTitle.setText(track.getTitle());
        txtArtist.setText(track.getArtist());

        updateTheme();
    }

    private void updateTheme() {
        SettingManager settingManager = SettingManager.getInstance(getContext());
        if (settingManager.isDarkTheme()) {
//            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight5));

            imgLibrary.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtLibrary.setTextColor(getResources().getColor(R.color.colorLight1));

            imgPlaylist.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtPlaylist.setTextColor(getResources().getColor(R.color.colorLight1));

            imgDownload.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtDownload.setTextColor(getResources().getColor(R.color.colorLight1));

            imgBroadcast.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtBroadcast.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
//            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark5));

            imgLibrary.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtLibrary.setTextColor(getResources().getColor(R.color.colorDark1));

            imgPlaylist.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtPlaylist.setTextColor(getResources().getColor(R.color.colorDark1));

            imgDownload.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtDownload.setTextColor(getResources().getColor(R.color.colorDark1));

            imgBroadcast.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtBroadcast.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    private void addEvent() {
        add_to_library_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PlaylistManager.getInstance().isContainInLikedSongs(track.getId())) {
                    PlaylistManager.getInstance().removeFromLikedSongs(track.getId());
                    Toast.makeText(getContext(), "Removed \"" + track.getTitle() + "\" from liked songs", Toast.LENGTH_LONG).show();
                } else {
                    PlaylistManager.getInstance().addToLikedSongs(track.getId());
                    Toast.makeText(getContext(), "Added \"" + track.getTitle() + "\" in liked songs", Toast.LENGTH_LONG).show();
                }

                closefragment();
            }
        });
    }

    private void closefragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private void handleCoverPlaceholderColor(RoundImageView imgCover) {
        if (settingManager.isDarkTheme()) {
            Glide.with(requireContext()).load(track.getCover()).placeholder(R.color.colorLight5).into(imgCover);
        } else {
            Glide.with(requireContext()).load(track.getCover()).placeholder(R.color.colorDark5).into(imgCover);
        }
    }
}
