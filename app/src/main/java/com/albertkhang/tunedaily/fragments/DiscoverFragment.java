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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.SettingsActivity;
import com.albertkhang.tunedaily.adapters.AlbumAdapter;
import com.albertkhang.tunedaily.adapters.TopChartAdapter;
import com.albertkhang.tunedaily.utils.Album;
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

public class DiscoverFragment extends Fragment {
    private ImageView imgSettings;
    private ImageView imgUser;
    private ShimmerFrameLayout shimmer_top_chart;
    private ShimmerFrameLayout shimmer_popular_albums;
    private ShimmerFrameLayout shimmer_best_of_artists;
    private RecyclerView rvTopChart;
    private RecyclerView rvPopularAlbums;
    private RecyclerView rvBestOfArtists;
    private TopChartAdapter topChartAdapter;
    private AlbumAdapter popularAlbumAdapter;
    private AlbumAdapter bestOfArtistAlbumAdapter;
    private LinearLayout root_view;
    private RelativeLayout top_frame;
    private TextView txtTopChart;
    private TextView txtPopularAlbum;
    private TextView txtBestOfArtist;
    private ImageView imgTopLogo;

    private SwipeRefreshLayout swipe_frame;

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
        imgUser = view.findViewById(R.id.imgUser);
        root_view = view.findViewById(R.id.root_view);
        top_frame = view.findViewById(R.id.top_frame);
        txtTopChart = view.findViewById(R.id.txtTopChart);
        txtPopularAlbum = view.findViewById(R.id.txtPopularAlbum);
        txtBestOfArtist = view.findViewById(R.id.txtBestOfArtist);
        imgTopLogo = view.findViewById(R.id.imgTopLogo);

        swipe_frame = view.findViewById(R.id.swipe_frame);

        shimmer_top_chart = view.findViewById(R.id.shimmer_top_chart);
        shimmer_popular_albums = view.findViewById(R.id.shimmer_popular_albums);
        shimmer_best_of_artists = view.findViewById(R.id.shimmer_best_of_artists);
        rvTopChart = view.findViewById(R.id.rvTopChart);
        rvPopularAlbums = view.findViewById(R.id.rvPopularAlbums);
        rvBestOfArtists = view.findViewById(R.id.rvBestOfArtists);

        //TopChart
        topChartAdapter = new TopChartAdapter(getContext());
        rvTopChart.setAdapter(topChartAdapter);

        LinearLayoutManager managerTopChart = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        managerTopChart.setOrientation(RecyclerView.VERTICAL);
        rvTopChart.setLayoutManager(managerTopChart);

        //Popular Albums
        popularAlbumAdapter = new AlbumAdapter(getContext());
        rvPopularAlbums.setAdapter(popularAlbumAdapter);

        LinearLayoutManager managerAlbums = new LinearLayoutManager(getContext());
        managerAlbums.setOrientation(RecyclerView.HORIZONTAL);
        rvPopularAlbums.setLayoutManager(managerAlbums);

        //BestOfArtists
        bestOfArtistAlbumAdapter = new AlbumAdapter(getContext());
        rvBestOfArtists.setAdapter(bestOfArtistAlbumAdapter);

        LinearLayoutManager managerArtist = new LinearLayoutManager(getContext());
        managerArtist.setOrientation(RecyclerView.HORIZONTAL);
        rvBestOfArtists.setLayoutManager(managerArtist);

        updateTheme();

        swipe_frame.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();
                swipe_frame.setRefreshing(false);
            }
        });

        updateList();
    }

    private void updateList() {
        updateTopChart();
        updatePopularAlbum();
        rvPopularAlbums.scrollToPosition(0);
        updateBestOfArtist();
        rvBestOfArtists.scrollToPosition(0);
    }

    private void updateBestOfArtist() {
        shimmer_best_of_artists.setVisibility(View.VISIBLE);
        rvBestOfArtists.setVisibility(View.GONE);

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.setReadBestOfArtistListener(new FirebaseManager.ReadBestOfArtistListener() {
            @Override
            public void readBestOfArtistListener(ArrayList<Album> albums) {
                shimmer_best_of_artists.setVisibility(View.GONE);
                rvBestOfArtists.setVisibility(View.VISIBLE);

                bestOfArtistAlbumAdapter.update(albums);
            }
        });

        firebaseManager.getBestOfArtist();
    }

    private void updatePopularAlbum() {
        shimmer_popular_albums.setVisibility(View.VISIBLE);
        rvPopularAlbums.setVisibility(View.GONE);

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.setReadPopularAlbumsListener(new FirebaseManager.ReadPopularAlbumsListener() {
            @Override
            public void readPopularAlbumsListener(ArrayList<Album> albums) {

                shimmer_popular_albums.setVisibility(View.GONE);
                rvPopularAlbums.setVisibility(View.VISIBLE);

                popularAlbumAdapter.update(albums);
            }
        });

        firebaseManager.getPopularAlbums();
    }

    private void updateTopChart() {
        shimmer_top_chart.setVisibility(View.VISIBLE);
        rvTopChart.setVisibility(View.GONE);

        final ArrayList ids = new ArrayList();

        final FirebaseManager firebaseManager = FirebaseManager.getInstance();
        firebaseManager.setReadTopTrackIdsListener(new FirebaseManager.ReadTopTrackIdsListener() {
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

                topChartAdapter.setOnMoreListener(new TopChartAdapter.OnMoreListener() {
                    @Override
                    public void onMoreListener(View view, int position) {
                        showMoreItem(tracks.get(position));
                    }
                });

                shimmer_top_chart.setVisibility(View.GONE);
                rvTopChart.setVisibility(View.VISIBLE);

                topChartAdapter.update(tracks);
            }
        });

        firebaseManager.getTopTrack();
    }

    private void updateTheme() {
        SettingManager manager = SettingManager.getInstance(getContext());
        if (manager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            imgTopLogo.setImageResource(R.drawable.ic_logo_and_text_dark);
            imgUser.setColorFilter(getResources().getColor(R.color.colorLight5));
            imgSettings.setColorFilter(getResources().getColor(R.color.colorLight5));

            txtTopChart.setTextColor(getResources().getColor(R.color.colorLight1));
            txtPopularAlbum.setTextColor(getResources().getColor(R.color.colorLight1));
            txtBestOfArtist.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            imgTopLogo.setImageResource(R.drawable.ic_logo_and_text_light);
            imgUser.setColorFilter(getResources().getColor(R.color.colorDark5));
            imgSettings.setColorFilter(getResources().getColor(R.color.colorDark5));

            txtTopChart.setTextColor(getResources().getColor(R.color.colorDark1));
            txtPopularAlbum.setTextColor(getResources().getColor(R.color.colorDark1));
            txtBestOfArtist.setTextColor(getResources().getColor(R.color.colorDark1));
        }

        topChartAdapter.notifyDataSetChanged();
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
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(track);
        trackMoreFragment.show(requireActivity().getSupportFragmentManager(), "FragmentTrackMore");
    }
}