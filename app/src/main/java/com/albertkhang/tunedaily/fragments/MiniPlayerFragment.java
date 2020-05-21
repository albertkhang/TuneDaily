package com.albertkhang.tunedaily.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
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
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.FullPlayerActivity;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.utils.UpdateThemeEvent;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.Serializable;

public class MiniPlayerFragment extends Fragment implements Serializable {
    private static final String LOG_TAG = "MiniPlayerFragment";

    public interface ACTION {
        String PLAY = "com.albertkhang.action.play";
    }

    private ConstraintLayout miniPlayer_background;
    private SettingManager settingManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

    private boolean isFavourite = false;
    private boolean isPlaying = false;

    private ImageView imgCover;
    private ImageView imgFavourite;
    private ImageView imgPlayPause;
    private ImageView imgSkipNext;
    private TextView txtTitle;
    private TextView txtArtist;
    private ConstraintLayout root_view;

    private MediaPlayer mediaPlayer;
    private Track currentTrack;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    private void addControl(View view) {
        imgCover = view.findViewById(R.id.imgCover);
        imgFavourite = view.findViewById(R.id.imgFavourite);
        imgPlayPause = view.findViewById(R.id.imgPlayPause);
        imgSkipNext = view.findViewById(R.id.imgSkipNext);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtArtist = view.findViewById(R.id.txtArtist);
        root_view = view.findViewById(R.id.root_view);

        miniPlayer_background = view.findViewById(R.id.miniPlayer_background);
        settingManager = SettingManager.getInstance(getContext());
    }

    private void addEvent() {
        imgFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "imgFavourite onClick");

                if (isFavourite) {
                    isFavourite = false;
                    imgFavourite.setImageResource(R.drawable.ic_favourite);
                } else {
                    isFavourite = true;
                    imgFavourite.setImageResource(R.drawable.ic_not_favourite);
                }
            }
        });

        imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "imgPlayPause onClick");
                updatePlayerStatus();
                updatePlayback();
            }
        });

        root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FullPlayerActivity.class);
                intent.putExtra("current_track", currentTrack);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
        updateCurrentTrack();
    }

    private void updateCurrentTrack() {
        if (currentTrack != null) {
            setCover(currentTrack.getCover());
            txtTitle.setText(currentTrack.getTitle());
            txtArtist.setText(currentTrack.getArtist());
        }
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            setMiniPlayerBackground(R.color.colorDark2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorLight1));

            imgCover.setImageResource(R.color.colorDark5);
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
        } else {
            setMiniPlayerBackground(R.color.colorLight2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorDark2));

            imgCover.setImageResource(R.color.colorLight5);
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
        }
    }

    private void setMiniPlayerBackground(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.round_corner_top_background);
        drawable.setTint(getResources().getColor(color));
        miniPlayer_background.setBackground(drawable);
    }

    private void play(Track track) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        if (mediaPlayer.isPlaying()) {
            pause();
        }

        try {
            mediaPlayer.setDataSource(track.getTrack());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.d("play", "e: " + e);
        }
    }

    private void setCover(String cover) {
        Glide.with(this).load(cover).placeholder(R.color.colorLight5).into(imgCover);
    }

    private void pause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(Track track) {
        currentTrack = new Track(track);

        setCover(track.getCover());
        txtTitle.setText(track.getTitle());
        txtArtist.setText(track.getArtist());

//        play(track);
        updatePlayback();
    }

    private void updatePlayerStatus() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    private void updatePlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                imgPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                imgPlayPause.setImageResource(R.drawable.ic_play);
            }
        }
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
