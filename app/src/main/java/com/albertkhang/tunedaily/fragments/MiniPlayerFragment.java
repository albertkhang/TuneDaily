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

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.FullPlayerActivity;

public class MiniPlayerFragment extends Fragment {
    private static final String LOG_TAG = "MiniPlayerFragment";

    private ConstraintLayout miniPlayer_background;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

    private boolean isFavourite = false;
    private boolean isPlaying = false;

    private ImageView imgFavourite;
    private ImageView imgPlayPause;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    private void addControl(View view) {
        imgFavourite = view.findViewById(R.id.imgFavourite);
        imgPlayPause = view.findViewById(R.id.imgPlayPause);
        miniPlayer_background = view.findViewById(R.id.miniPlayer_background);
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
}
