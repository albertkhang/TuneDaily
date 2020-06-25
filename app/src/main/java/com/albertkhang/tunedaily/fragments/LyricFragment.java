package com.albertkhang.tunedaily.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.models.Track;

import java.io.Serializable;

public class LyricFragment extends Fragment implements Serializable {
    private SettingManager settingManager;

    private FrameLayout top_gradient_frame;
    private FrameLayout bottom_gradient_frame;

    private TextView txtSongTitle;
    private TextView txtTitle;

    private TextView txtSingerTitle;
    private TextView txtArtist;

    private TextView txtLyric;

    private MiniPlayerFragment miniPlayerFragment;

    private Track currentTrack;

    public LyricFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lyric, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        updateTheme();
        updateDataIntent();
        addEvent();
    }

    private void updateDataIntent() {
        currentTrack = MediaPlaybackService.getCurrentTrack();

        if (currentTrack != null) {
            txtTitle.setText(currentTrack.getTitle());
            txtArtist.setText(currentTrack.getArtist());
        }
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());
        top_gradient_frame = view.findViewById(R.id.top_gradient_frame);
        bottom_gradient_frame = view.findViewById(R.id.bottom_gradient_frame);
        txtSongTitle = view.findViewById(R.id.txtSongTitle);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtSingerTitle = view.findViewById(R.id.txtSingerTitle);
        txtArtist = view.findViewById(R.id.txtArtist);
        txtLyric = view.findViewById(R.id.txtLyric);
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            top_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_top_gradient_dark);
            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_dark);

            txtSongTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            txtSingerTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight1));

            txtLyric.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            top_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_top_gradient_light);
            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_light);

            txtSongTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            txtSingerTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark1));

            txtLyric.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    private void addEvent() {

    }
}
