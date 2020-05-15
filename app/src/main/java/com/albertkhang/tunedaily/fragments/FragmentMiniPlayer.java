package com.albertkhang.tunedaily.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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

public class FragmentMiniPlayer extends Fragment {
    private static final String LOG_TAG = "MiniPlayerFragment";

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

        miniPlayer_background = view.findViewById(R.id.miniPlayer_background);
        settingManager = new SettingManager(getContext());
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

                if (isPlaying) {
                    isPlaying = false;
                    imgPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    isPlaying = true;
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        miniPlayer_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FullPlayerActivity.class);
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
            setMiniPlayerBackground(R.color.colorDark2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorLight1));

            imgCover.setImageResource(R.color.colorDark5);
        } else {
            setMiniPlayerBackground(R.color.colorLight2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark3));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorDark3));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorDark3));

            imgCover.setImageResource(R.color.colorLight5);
        }
    }

    private void setMiniPlayerBackground(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.round_corner_top_background);
        drawable.setTint(getResources().getColor(color));
        miniPlayer_background.setBackground(drawable);
    }
}
