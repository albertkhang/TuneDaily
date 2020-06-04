package com.albertkhang.tunedaily.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.events.UpdatePlaylistLibrary;
import com.albertkhang.tunedaily.utils.Playlist;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

public class PlaylistMoreFragment extends BottomSheetDialogFragment {
    private Playlist playlist;
    private RoundImageView imgCover;
    private TextView txtTitle;
    private ConstraintLayout delete_playlist_frame;
    private SettingManager settingManager = SettingManager.getInstance(getContext());

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
        txtTitle.setText(playlist.getTitle());

        if (playlist.getTitle().equals(getString(R.string.liked_songs))) {
            Glide.with(this).load(playlist.getCover()).placeholder(R.drawable.ic_favourite_red).into(imgCover);
        } else {
            Glide.with(this).load(playlist.getCover()).placeholder(R.drawable.ic_playlist_cover).into(imgCover);
        }

        if (getArguments().getBoolean("isShowDeletePlaylist")) {
            delete_playlist_frame.setVisibility(View.VISIBLE);
        } else {
            delete_playlist_frame.setVisibility(View.GONE);
        }
    }

    private void addControl(View view) {
        imgCover = view.findViewById(R.id.imgCover);
        txtTitle = view.findViewById(R.id.txtTitle);
        delete_playlist_frame = view.findViewById(R.id.delete_playlist_frame);
    }

    private void addEvent() {
        delete_playlist_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDelete();
            }
        });
    }

    private void showConfirmDelete() {
        final Dialog dialog = new Dialog(getContext(), R.style.RoundCornerDialogFragment);
        dialog.setContentView(R.layout.fragment_confirm_sign_out);

        TextView txtTitle = dialog.findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.confirm_delete_playlist);

        Button cancel = dialog.findViewById(R.id.btnCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        Button delete = dialog.findViewById(R.id.btnCreate);
        delete.setText(R.string.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaylistManager.getInstance().deletePlaylist(playlist.getTitle());
                Toast.makeText(getContext(), "Deleted " + playlist.getTitle(), Toast.LENGTH_LONG).show();
                closefragment();
                EventBus.getDefault().post(new UpdatePlaylistLibrary());
                dialog.dismiss();
            }
        });

        if (settingManager.isDarkTheme()) {
            dialog.findViewById(R.id.root_view).setBackgroundResource(R.drawable.round_dark_dialog_background);
            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            cancel.setBackgroundResource(R.drawable.round_dark_dialog_button);
            delete.setBackgroundResource(R.drawable.round_dark_dialog_button);

            cancel.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            dialog.findViewById(R.id.root_view).setBackgroundResource(R.drawable.round_light_dialog_background);
            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            cancel.setBackgroundResource(R.drawable.round_light_dialog_button);
            delete.setBackgroundResource(R.drawable.round_light_dialog_button);

            cancel.setTextColor(getResources().getColor(R.color.colorDark1));
        }

        dialog.show();
    }

    private void closefragment() {
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).commit();
    }
}
