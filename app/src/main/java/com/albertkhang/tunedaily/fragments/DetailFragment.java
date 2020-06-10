package com.albertkhang.tunedaily.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.TrackAdapter;
import com.albertkhang.tunedaily.events.UpdateCurrentTrackEvent;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;

public class DetailFragment extends Fragment implements Serializable {
    private static final String LOG_TAG = "DetailFragment";

    private SettingManager settingManager;
    private FrameLayout top_gradient_frame;
    private ConstraintLayout detail_frame;

    private TextView txtSongTitle;
    private TextView txtAlbumTitle;
    private TextView txtArtistTitle;
    private TextView txtGenreTitle;
    private TextView txtComposerTitle;

    private TextView txtSong;
    private TextView txtAlbum;
    private TextView txtArtist;
    private TextView txtGenre;
    private TextView txtComposer;

    private TextView txtSimilarSongs;

    private FrameLayout bottom_gradient_frame;

    private ShimmerFrameLayout shimmer_similar_songs;
    private RecyclerView rvSimilarSongs;
    private TrackAdapter similarSongsAdapter;

    private Track currentTrack;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        updateDataIntent();
        addEvent();
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());

        top_gradient_frame = view.findViewById(R.id.top_gradient_frame);
        detail_frame = view.findViewById(R.id.detail_frame);

        txtSongTitle = view.findViewById(R.id.txtSongTitle);
        txtAlbumTitle = view.findViewById(R.id.txtAlbumTitle);
        txtArtistTitle = view.findViewById(R.id.txtArtistTitle);
        txtGenreTitle = view.findViewById(R.id.txtGenreTitle);
        txtComposerTitle = view.findViewById(R.id.txtComposerTitle);

        txtSong = view.findViewById(R.id.txtSong);
        txtAlbum = view.findViewById(R.id.txtAlbum);
        txtArtist = view.findViewById(R.id.txtArtist);
        txtGenre = view.findViewById(R.id.txtGenre);
        txtComposer = view.findViewById(R.id.txtComposer);

        txtSimilarSongs = view.findViewById(R.id.txtSimilarSongs);

        bottom_gradient_frame = view.findViewById(R.id.bottom_gradient_frame);

        shimmer_similar_songs = view.findViewById(R.id.shimmer_similar_songs);
        rvSimilarSongs = view.findViewById(R.id.rvSimilarSongs);

        similarSongsAdapter = new TrackAdapter(getContext());
        rvSimilarSongs.setAdapter(similarSongsAdapter);

        LinearLayoutManager managerSongs = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        managerSongs.setOrientation(LinearLayoutManager.VERTICAL);
        rvSimilarSongs.setLayoutManager(managerSongs);
        rvSimilarSongs.setNestedScrollingEnabled(false);

        updateTheme();
//        updateSimilarSongs();
        updateCurrentPlaylist();
    }

    private void updateCurrentPlaylist() {
        shimmer_similar_songs.setVisibility(View.GONE);
        rvSimilarSongs.setVisibility(View.VISIBLE);
        similarSongsAdapter.update(MediaPlaybackService.getCurrentPlaylist());
    }

    private void updateSimilarSongs() {
        shimmer_similar_songs.setVisibility(View.VISIBLE);
        rvSimilarSongs.setVisibility(View.GONE);

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.setReadRandomSongsListener(new FirebaseManager.ReadRandomSongsListener() {
            @Override
            public void readRandomSongsListener(final ArrayList<Track> tracks) {
                shimmer_similar_songs.setVisibility(View.GONE);
                rvSimilarSongs.setVisibility(View.VISIBLE);

                similarSongsAdapter.setOnMoreListener(new TrackAdapter.OnMoreListener() {
                    @Override
                    public void onMoreListener(View view, int position) {
                        showMoreItem(tracks.get(position));
                    }
                });

                similarSongsAdapter.update(tracks);
            }
        });

        firebaseManager.getRandomSongs();
    }

    private void showMoreItem(Track track) {
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(track);
        trackMoreFragment.show(requireActivity().getSupportFragmentManager(), "FragmentTrackMore");
    }

    private void addEvent() {
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            top_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_top_gradient_dark);

            detail_frame.setBackgroundResource(R.drawable.round_dark_background);

            txtSongTitle.setTextColor(getResources().getColor(R.color.colorLight4));
            txtAlbumTitle.setTextColor(getResources().getColor(R.color.colorLight4));
            txtArtistTitle.setTextColor(getResources().getColor(R.color.colorLight4));
            txtGenreTitle.setTextColor(getResources().getColor(R.color.colorLight4));
            txtComposerTitle.setTextColor(getResources().getColor(R.color.colorLight4));

            txtSong.setTextColor(getResources().getColor(R.color.colorLight1));
            txtAlbum.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight1));
            txtGenre.setTextColor(getResources().getColor(R.color.colorLight1));
            txtComposer.setTextColor(getResources().getColor(R.color.colorLight1));

            txtSimilarSongs.setTextColor(getResources().getColor(R.color.colorLight1));

            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_dark);
        } else {
            top_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_top_gradient_light);

            detail_frame.setBackgroundResource(R.drawable.round_light_background);

            txtSongTitle.setTextColor(getResources().getColor(R.color.colorDark4));
            txtAlbumTitle.setTextColor(getResources().getColor(R.color.colorDark4));
            txtArtistTitle.setTextColor(getResources().getColor(R.color.colorDark4));
            txtGenreTitle.setTextColor(getResources().getColor(R.color.colorDark4));
            txtComposerTitle.setTextColor(getResources().getColor(R.color.colorDark4));

            txtSong.setTextColor(getResources().getColor(R.color.colorDark1));
            txtAlbum.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark1));
            txtGenre.setTextColor(getResources().getColor(R.color.colorDark1));
            txtComposer.setTextColor(getResources().getColor(R.color.colorDark1));

            txtSimilarSongs.setTextColor(getResources().getColor(R.color.colorDark1));

            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_light);
        }
    }

    private void updateDataIntent() {
        currentTrack = MediaPlaybackService.getCurrentTrack();

        if (currentTrack != null) {
            txtSong.setText(currentTrack.getTitle());
            txtAlbum.setText(currentTrack.getAlbum());
            txtArtist.setText(currentTrack.getArtist());
            txtGenre.setText(currentTrack.getGenre());
            txtComposer.setText("N/A");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateCurrentTrackEvent updateCurrentTrackEvent) {
        Log.d(LOG_TAG, "onPlayAction: " + updateCurrentTrackEvent.getTrack().toString());

        txtSong.setText(updateCurrentTrackEvent.getTrack().getTitle());
        txtAlbum.setText(updateCurrentTrackEvent.getTrack().getAlbum());
        txtArtist.setText(updateCurrentTrackEvent.getTrack().getArtist());
        txtGenre.setText(updateCurrentTrackEvent.getTrack().getGenre());
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
