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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.InSearchActivity;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.UpdateThemeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FragmentSearch extends Fragment {
    private FrameLayout search_frame;
    private ConstraintLayout root_view;
    private SettingManager settingManager;
    private TextView txtSearchTitle;
    private ImageView imgSearch;
    private TextView txtSearchText;
    private TextView txtRandomArtist;
    private TextView txtRandomTracks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateTheme();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
    }

    private void addControl(View view) {
        search_frame = view.findViewById(R.id.search_frame);
        root_view = view.findViewById(R.id.root_view);
        txtSearchTitle = view.findViewById(R.id.txtSearchTitle);
        imgSearch = view.findViewById(R.id.imgSearch);
        txtSearchText = view.findViewById(R.id.txtSearchText);
        txtRandomArtist = view.findViewById(R.id.txtRandomArtist);
        txtRandomTracks = view.findViewById(R.id.txtRandomTracks);

        settingManager = SettingManager.getInstance(getContext());

        updateTheme();
    }

    private void addEvent() {
        search_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), InSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
            txtSearchTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            setSearchBackground(R.color.colorLight1);
            imgSearch.setColorFilter(getResources().getColor(R.color.colorDark1));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorDark1));

            txtRandomArtist.setTextColor(getResources().getColor(R.color.colorLight1));
            txtRandomTracks.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
            txtSearchTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            setSearchBackground(R.color.colorDark1);
            imgSearch.setColorFilter(getResources().getColor(R.color.colorLight1));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorLight1));

            txtRandomArtist.setTextColor(getResources().getColor(R.color.colorDark1));
            txtRandomTracks.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    private void setSearchBackground(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.search_background);
        drawable.setTint(getResources().getColor(color));
        search_frame.setBackground(drawable);
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
