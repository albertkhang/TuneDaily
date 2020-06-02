package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.TrackAdapter;
import com.albertkhang.tunedaily.fragments.MiniPlayerFragment;
import com.albertkhang.tunedaily.fragments.PlaylistMoreFragment;
import com.albertkhang.tunedaily.fragments.TrackMoreFragment;
import com.albertkhang.tunedaily.utils.Album;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.Playlist;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.Serializable;
import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity implements Serializable {
    private SettingManager settingManager;

    private ConstraintLayout root_view;
    private ConstraintLayout top_frame;
    private ImageView imgCollapse;
    private TextView txtTitle;
    private ImageView imgMore;
    private FrameLayout top_gradient_frame;
    private FrameLayout bottom_gradient_frame;

    private RecyclerView rvTracks;
    private TrackAdapter trackAdapter;
    private ShimmerFrameLayout shimmer_random_songs;
    private TextView txtEmpty;
    private FrameLayout miniPlayer_frame;

    private ArrayList<Integer> tracks;

    public interface TYPE {
        String PLAYLIST = "playlist";
        String ALBUM = "album";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        addControl();
        addMiniPlayer();
        addEvent();
    }

    private void addMiniPlayer() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.miniPlayer_frame, new MiniPlayerFragment()).commit();
    }

    private void addControl() {
        settingManager = SettingManager.getInstance(this);

        root_view = findViewById(R.id.root_view);
        top_frame = findViewById(R.id.top_frame);
        txtTitle = findViewById(R.id.txtTitle);
        imgMore = findViewById(R.id.imgMore);
        imgCollapse = findViewById(R.id.imgCollapse);
        top_gradient_frame = findViewById(R.id.top_gradient_frame);
        bottom_gradient_frame = findViewById(R.id.bottom_gradient_frame);
        rvTracks = findViewById(R.id.rvTracks);
        shimmer_random_songs = findViewById(R.id.shimmer_random_songs);
        txtEmpty = findViewById(R.id.txtEmpty);
        miniPlayer_frame = findViewById(R.id.miniPlayer_frame);

        trackAdapter = new TrackAdapter(this);
        rvTracks.setAdapter(trackAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvTracks.setLayoutManager(linearLayoutManager);

        updateIntentData();
        updateTrackList();
    }

    private void updateTrackList() {
        shimmer_random_songs.setVisibility(View.VISIBLE);
        rvTracks.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        if (tracks.size() != 0) {
            FirebaseManager firebaseManager = FirebaseManager.getInstance();

            firebaseManager.setReadTrackFromIdsListener(new FirebaseManager.ReadTrackFromIdsListener() {
                @Override
                public void readTrackFromIdsListener(final ArrayList<Track> tracks) {
                    shimmer_random_songs.setVisibility(View.GONE);
                    rvTracks.setVisibility(View.VISIBLE);
                    txtEmpty.setVisibility(View.GONE);

                    trackAdapter.setOnMoreListener(new TrackAdapter.OnMoreListener() {
                        @Override
                        public void onMoreListener(View view, int position) {
                            showMoreItem(tracks.get(position));
                        }
                    });

                    trackAdapter.update(tracks);
                }
            });

            firebaseManager.getTrackFromIds(tracks);
        } else {
            shimmer_random_songs.setVisibility(View.GONE);
            rvTracks.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void showMoreItem(Track track) {
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(track);
        trackMoreFragment.show(getSupportFragmentManager(), "FragmentTrackMore");
    }

    private void showMoreItem(Playlist playlist) {
        PlaylistMoreFragment playlistMoreFragment = new PlaylistMoreFragment(playlist);
        playlistMoreFragment.show(getSupportFragmentManager(), "PlaylistMoreFragment");
    }

    private void updateIntentData() {
        String type = getIntent().getStringExtra("type");

        switch (type) {
            case TYPE.PLAYLIST:
                final String name = getIntent().getStringExtra("name");
                final String cover = getIntent().getStringExtra("cover");
                Log.d("updateIntentData", "name: " + name);

                txtTitle.setText(name);

                tracks = PlaylistManager.getInstance().getPlaylistTracks(name);
                Log.d("updateIntentData", tracks.toString());

                imgMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMoreItem(new Playlist(name, -1, cover));
                    }
                });

                break;

            case TYPE.ALBUM:
                final Album album = (Album) getIntent().getSerializableExtra("album");
                Log.d("updateIntentData", album.toString());
                txtTitle.setText(album.getTitle());

                tracks = (ArrayList<Integer>) album.getTracks();

                imgMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMoreItem(new Playlist(album.getTitle(), -1, album.getCover()));
                    }
                });
                break;
        }
    }

    private void addEvent() {
        imgCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            imgMore.setColorFilter(getResources().getColor(R.color.colorLight5));

            top_gradient_frame.setBackground(getResources().getDrawable(R.drawable.lyric_hidden_top_gradient_dark));
            bottom_gradient_frame.setBackground(getResources().getDrawable(R.drawable.lyric_hidden_bottom_gradient_dark));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            imgMore.setColorFilter(getResources().getColor(R.color.colorDark5));

            top_gradient_frame.setBackground(getResources().getDrawable(R.drawable.lyric_hidden_top_gradient_light));
            bottom_gradient_frame.setBackground(getResources().getDrawable(R.drawable.lyric_hidden_bottom_gradient_light));
        }
    }
}
