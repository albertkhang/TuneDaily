package com.albertkhang.tunedaily.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
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
import com.albertkhang.tunedaily.utils.SettingManager;

public class FragmentFullPlayer extends Fragment {
    private SettingManager settingManager;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_full_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());

        seekbar = view.findViewById(R.id.seekbar);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtArtist = view.findViewById(R.id.txtArtist);
        txtTimeStampStart = view.findViewById(R.id.txtTimeStampStart);
        txtTimeStampEnd = view.findViewById(R.id.txtTimeStampEnd);

        imgSkipPrevious = view.findViewById(R.id.imgSkipPrevious);
        imgPlayPause = view.findViewById(R.id.imgPlayPause);
        imgSkipNext = view.findViewById(R.id.imgSkipNext);
        imgFavourite = view.findViewById(R.id.imgFavourite);
        imgRepeat = view.findViewById(R.id.imgRepeat);

        updateTheme();
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

    }
}

