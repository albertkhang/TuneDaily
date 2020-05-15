package com.albertkhang.tunedaily.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.SettingsActivity;
import com.albertkhang.tunedaily.adapters.TopChartAdapter;
import com.albertkhang.tunedaily.adapters.ViewPagerAdapter;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.TopTrack;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.utils.UpdateThemeEvent;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentDiscover extends Fragment {
    private ImageView imgSettings;
    private ImageView imgUser;
    private ShimmerFrameLayout shimmer_top_chart;
    private RecyclerView rvTopChart;
    private TopChartAdapter topChartAdapter;
    private LinearLayout root_view;
    private RelativeLayout top_frame;
    private TextView txtTopChart;
    private TextView txtPopularAlbum;
    private TextView txtBestOfArtist;
    private ImageView imgTopLogo;

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        updateTheme();
    }

    private void addControl(View view) {
        imgSettings = view.findViewById(R.id.imgSettings);
        imgUser = view.findViewById(R.id.imgUser);
        root_view = view.findViewById(R.id.root_view);
        top_frame = view.findViewById(R.id.top_frame);
        txtTopChart = view.findViewById(R.id.txtTopChart);
        txtPopularAlbum = view.findViewById(R.id.txtPopularAlbum);
        txtBestOfArtist = view.findViewById(R.id.txtBestOfArtist);
        imgTopLogo = view.findViewById(R.id.imgTopLogo);

        shimmer_top_chart = view.findViewById(R.id.shimmer_top_chart);
        rvTopChart = view.findViewById(R.id.rvTopChart);
        topChartAdapter = new TopChartAdapter(getContext());
        rvTopChart.setAdapter(topChartAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        manager.setOrientation(RecyclerView.VERTICAL);
        rvTopChart.setLayoutManager(manager);

        final ArrayList ids = new ArrayList();

        final FirebaseManager firebaseManager = FirebaseManager.getInstance();
        firebaseManager.setUpdateTopTrackIdsListener(new FirebaseManager.UpdateTopTrackIdsListener() {
            @Override
            public void updateTopTrackListener(ArrayList<TopTrack> topChartIdsOrdered) {
                for (TopTrack track : topChartIdsOrdered) {
                    ids.add(track.getId());
                }

                firebaseManager.getTrackFromIds(ids);
                Log.d("firebaseManager", "ids: " + ids.toString());
            }
        });

        firebaseManager.setReadTrackFromIdsListener(new FirebaseManager.ReadTrackFromIdsListener() {
            @Override
            public void readTrackFromIdsListener(final ArrayList<Track> tracks) {
                for (Track track : tracks) {
                    Log.d("firebaseManager", track.toString());
                }

                topChartAdapter.setOnItemClickListener(new TopChartAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        showMoreItem(tracks.get(position));
                    }
                });

                shimmer_top_chart.setVisibility(View.GONE);
                rvTopChart.setVisibility(View.VISIBLE);
                topChartAdapter.updateTopTracks(tracks);
            }
        });

        firebaseManager.getTopTrack();

        updateTheme();
    }

    private void updateTheme() {
        SettingManager manager = new SettingManager(getContext());
        if (manager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            imgTopLogo.setImageResource(R.drawable.ic_logo_and_text_dark);
            imgUser.setColorFilter(getResources().getColor(R.color.colorLight3));
            imgSettings.setColorFilter(getResources().getColor(R.color.colorLight3));

            txtTopChart.setTextColor(getResources().getColor(R.color.colorLight1));
            txtPopularAlbum.setTextColor(getResources().getColor(R.color.colorLight1));
            txtBestOfArtist.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            imgTopLogo.setImageResource(R.drawable.ic_logo_and_text_light);
            imgUser.setColorFilter(getResources().getColor(R.color.colorDark3));
            imgSettings.setColorFilter(getResources().getColor(R.color.colorDark3));

            txtTopChart.setTextColor(getResources().getColor(R.color.colorDark1));
            txtPopularAlbum.setTextColor(getResources().getColor(R.color.colorDark1));
            txtBestOfArtist.setTextColor(getResources().getColor(R.color.colorDark1));
        }
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

    @Override
    public void onResume() {
        super.onResume();
        topChartAdapter.notifyDataSetChanged();
        updateTheme();
    }

    private void addEvent(View view) {
        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.animation_start_enter, R.anim.animation_start_leave);
            }
        });

//        imgUser.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentTrackMore fragmentTrackMore = new FragmentTrackMore();
//                fragmentTrackMore.show(requireActivity().getSupportFragmentManager(), "FragmentTrackMore");
//            }
//        });
    }

    private void showMoreItem(Track track) {
        FragmentTrackMore fragmentTrackMore = new FragmentTrackMore(track);
        fragmentTrackMore.show(requireActivity().getSupportFragmentManager(), "FragmentTrackMore");
    }
}