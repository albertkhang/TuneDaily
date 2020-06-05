package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
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
import com.albertkhang.tunedaily.services.MediaPlaybackConnectHelper;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.Playlist;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.Serializable;
import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity implements Serializable {
    private SettingManager settingManager;
    public static final String MINI_PLAYER_TAG = "com.albertkhang.activities.playlistactivity.miniplayer";
    public static final String LOG_TAG = "PlaylistActivity";

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
    private Playlist currentPlaylist;

    private MediaPlaybackConnectHelper connectHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        addControl();
        addMiniPlayer();
        addEvent();
    }

    private void addMiniPlayer() {
        miniPlayer_frame.setVisibility(View.GONE);
        final MiniPlayerFragment miniPlayerFragment = new MiniPlayerFragment();

        connectHelper.setOnPlayingListener(new MediaPlaybackConnectHelper.OnPlayingListener() {
            @Override
            public void onPlayingListener(boolean isPlaying) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(MINI_PLAYER_TAG);

                if (fragment == null) {
                    if (isPlaying) {
                        miniPlayer_frame.setVisibility(View.VISIBLE);

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isPlaying", true);
                        miniPlayerFragment.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.miniPlayer_frame, miniPlayerFragment, MINI_PLAYER_TAG).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.miniPlayer_frame, miniPlayerFragment, MINI_PLAYER_TAG).commit();
                    }
                }
            }
        });
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

        connectHelper = new MediaPlaybackConnectHelper(this);

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
                            showTrackMoreItem(tracks.get(position));
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

    private void showTrackMoreItem(Track track) {
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(track);
        trackMoreFragment.show(getSupportFragmentManager(), "FragmentTrackMore");
    }

    private void showPlaylistMore(Playlist playlist) {
        PlaylistMoreFragment playlistMoreFragment = new PlaylistMoreFragment(playlist);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isShowDeletePlaylist", false);
        playlistMoreFragment.setArguments(bundle);
        playlistMoreFragment.show(getSupportFragmentManager(), "PlaylistMoreFragment");
    }

    private void updateIntentData() {
        Bundle bundle = getIntent().getBundleExtra("ids");
        tracks = bundle.getIntegerArrayList("ids");
        String title = getIntent().getStringExtra("title");

        String cover = getIntent().getStringExtra("cover");
        if (cover != null) {
            currentPlaylist = new Playlist(-1, title, cover, tracks);
        } else {
            currentPlaylist = new Playlist(-1, title, getString(R.string.liked_songs), tracks);
        }

        if (title != null) {
            txtTitle.setText(title);
        }
        Log.d("PlaylistActivity", "tracks: " + tracks.toString());
    }

    private void addEvent() {
        imgCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaylistMore(currentPlaylist);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
        connectHelper.putInOnResume();
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

    @Override
    protected void onStart() {
        super.onStart();
        connectHelper.putInOnStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectHelper.putInOnStop();
    }
}
