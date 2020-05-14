package com.albertkhang.tunedaily.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.SettingsActivity;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.TopTrack;
import com.albertkhang.tunedaily.utils.Track;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentDiscover extends Fragment {
    private ImageView imgSettings;
    private ShimmerFrameLayout shimmer_top_chart;
    private RecyclerView rvTopChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent(view);
    }

    private void addControl(View view) {
        imgSettings = view.findViewById(R.id.imgSettings);

        shimmer_top_chart = view.findViewById(R.id.shimmer_top_chart);
        rvTopChart = view.findViewById(R.id.rvTopChart);

        final ArrayList ids = new ArrayList();

        final FirebaseManager firebaseManager = FirebaseManager.getInstance();
        firebaseManager.setUpdateTopTrackIds(new FirebaseManager.UpdateTopTrackIds() {
            @Override
            public void updateTopTrack(ArrayList<TopTrack> topChartIdsOrdered) {
                for (TopTrack track : topChartIdsOrdered) {
                    ids.add(track.getId());
                }

                firebaseManager.getTrackFromIds(ids);
                Log.d("firebaseManager", "ids: " + ids.toString());
            }
        });

        firebaseManager.setReadTrackFromIds(new FirebaseManager.ReadTrackFromIds() {
            @Override
            public void readTrackFromIds(ArrayList<Track> tracks) {
                for (Track track : tracks) {
                    Log.d("firebaseManager", track.toString());
                }
            }
        });

        firebaseManager.getTopTrack();
    }

    private void addEvent(View view) {
        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.animation_start_enter, R.anim.animation_start_leave);
            }
        });
    }
}