package com.albertkhang.tunedaily.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.services.MediaPlaybackConnectHelper;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.TimeConverter;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.views.CircleImageView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

public class FullPlayerFragment extends Fragment implements Serializable {
    private SettingManager settingManager;
    private CircleImageView imgCover;
    private TextView txtTitle;
    private TextView txtArtist;
    private SeekBar seekbar;
    private TextView txtTimeStampStart;
    private TextView txtTimeStampEnd;
    private ImageView imgSkipPrevious;
    private ImageView imgPlayPause;
    private ImageView imgSkipNext;
    private ImageView imgFavourite;
    private ImageView imgRepeat;

    private Track currentTrack;

    private MediaPlaybackConnectHelper connectHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentTrack = (Track) getArguments().getSerializable("current_track");
        return inflater.inflate(R.layout.fragment_full_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        updateDataIntent();
        addEvent();
    }

    private void updateDataIntent() {
        Glide.with(this).load(currentTrack.getCover()).into(imgCover);
        txtTitle.setText(currentTrack.getTitle());
        txtArtist.setText(currentTrack.getArtist());

        txtTimeStampEnd.setText(TimeConverter.getInstance().getTimestamp(currentTrack.getDuration()));
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());

        seekbar = view.findViewById(R.id.seekbar);
        imgCover = view.findViewById(R.id.imgCover);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtArtist = view.findViewById(R.id.txtArtist);
        txtTimeStampStart = view.findViewById(R.id.txtTimeStampStart);
        txtTimeStampEnd = view.findViewById(R.id.txtTimeStampEnd);

        imgSkipPrevious = view.findViewById(R.id.imgSkipPrevious);
        imgPlayPause = view.findViewById(R.id.imgPlayPause);
        imgSkipNext = view.findViewById(R.id.imgSkipNext);
        imgFavourite = view.findViewById(R.id.imgFavourite);
        imgRepeat = view.findViewById(R.id.imgRepeat);

        connectHelper = new MediaPlaybackConnectHelper(getActivity());

        updateTheme();
        initialOnPlayingListener();
    }

    private void initialOnPlayingListener() {
        connectHelper.setOnPlayingListener(new MediaPlaybackConnectHelper.OnPlayingListener() {
            @Override
            public void onPlayingListener(boolean isPlaying) {
                if (isPlaying) {
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                } else {
                    imgPlayPause.setImageResource(R.drawable.ic_play);
                }
            }
        });
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight4));

            seekbar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorDark2)));

            txtTimeStampStart.setTextColor(getResources().getColor(R.color.colorLight5));
            txtTimeStampEnd.setTextColor(getResources().getColor(R.color.colorLight5));

            imgRepeat.setColorFilter(getResources().getColor(R.color.colorLight5));
            imgSkipPrevious.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight1));
        } else {
            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark4));

            seekbar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorLight2)));

            txtTimeStampStart.setTextColor(getResources().getColor(R.color.colorDark5));
            txtTimeStampEnd.setTextColor(getResources().getColor(R.color.colorDark5));

            imgRepeat.setColorFilter(getResources().getColor(R.color.colorDark5));
            imgSkipPrevious.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark2));
        }
    }

    private void addEvent() {
        imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat controller = connectHelper.getMediaController();
                int state = controller.getPlaybackState().getState();
                if (state == PlaybackStateCompat.STATE_PLAYING) {
                    controller.getTransportControls().pause();
                    imgPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    controller.getTransportControls().play();
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveTrack(Track track) {
        Glide.with(this).load(track.getCover()).into(imgCover);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        connectHelper.putInOnStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        connectHelper.putInOnResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        connectHelper.putInOnStop();
    }
}

