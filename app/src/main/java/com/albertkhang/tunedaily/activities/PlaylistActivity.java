package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.TrackAdapter;
import com.albertkhang.tunedaily.callbacks.SwipeToDeleteCallback;
import com.albertkhang.tunedaily.events.ShowMiniplayerEvent;
import com.albertkhang.tunedaily.events.UpdateDownloadedTrack;
import com.albertkhang.tunedaily.events.UpdateFavouriteTrack;
import com.albertkhang.tunedaily.fragments.MiniPlayerFragment;
import com.albertkhang.tunedaily.fragments.PlaylistMoreFragment;
import com.albertkhang.tunedaily.fragments.TrackMoreFragment;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.models.Playlist;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.models.Track;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private FrameLayout shuffle_play_frame;

    private ArrayList<Integer> trackIds;
    private static ArrayList<Track> trackList;
    private Playlist currentPlaylist;

    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback connectionCallback;
    private MediaControllerCompat.Callback controllerCallback;
    private MediaControllerCompat mediaController;

    private Track mRecentlyDeletedTrack;
    private int mRecentlyDeletedTrackPosition;
    private int mRecentlyDeletedIdPosition;

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

        getSupportFragmentManager().beginTransaction()
                .add(R.id.miniPlayer_frame, new MiniPlayerFragment(), MINI_PLAYER_TAG).commit();
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
        shuffle_play_frame = findViewById(R.id.shuffle_play_frame);

        trackAdapter = new TrackAdapter(this);
        rvTracks.setAdapter(trackAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvTracks.setLayoutManager(linearLayoutManager);

        mediaController = MediaControllerCompat.getMediaController(this);

        initialControllerCallback();
        initialConnectionCallback();
        initialMediaBrowser();

        updateIntentData();
        updateTrackList();
    }

    private void initialMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MediaPlaybackService.class),
                connectionCallback,
                null); // optional Bundle
    }

    private void initialConnectionCallback() {
        connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                super.onConnected();
//                Log.d(LOG_TAG, "onConnected");

                // Get the token for the MediaSession
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
//                Log.d(LOG_TAG, "token: " + token.toString());

                // Create a MediaControllerCompat
                try {
                    mediaController =
                            new MediaControllerCompat(PlaylistActivity.this, // Context
                                    token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(PlaylistActivity.this, mediaController);

                    // Finish building the UI
                    buildTransportControls();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void buildTransportControls() {
        // Display the initial state
//        Log.d(LOG_TAG, "Display the initial state");
        if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            miniPlayer_frame.setVisibility(View.VISIBLE);
        }

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    private void initialControllerCallback() {
//        Log.d(LOG_TAG, "initialControllerCallback");
        controllerCallback = new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                super.onMetadataChanged(metadata);
                String cover = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
                String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
                String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                Log.d(LOG_TAG, "cover: " + cover);
                Log.d(LOG_TAG, "title: " + title);
                Log.d(LOG_TAG, "artist: " + artist);
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    Log.d(LOG_TAG, "controllerCallback STATE_PLAYING");
                } else {
                    Log.d(LOG_TAG, "controllerCallback STATE_PAUSED");
                }
            }
        };
    }

    private void updateTrackList() {
        shimmer_random_songs.setVisibility(View.VISIBLE);
        rvTracks.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        if (trackIds.size() != 0) {
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
                    trackList = tracks;
                }
            });

            firebaseManager.getTrackFromIds(trackIds);
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
        trackIds = bundle.getIntegerArrayList("ids");
        String title = getIntent().getStringExtra("title");

        String cover = getIntent().getStringExtra("cover");
        if (cover != null) {
            currentPlaylist = new Playlist(-1, title, cover, trackIds);
        } else {
            currentPlaylist = new Playlist(-1, title, getString(R.string.liked_songs), trackIds);
        }

        if (title != null) {
            txtTitle.setText(title);
        }
        Log.d("PlaylistActivity", "tracks: " + trackIds.toString());
    }

    private void addEvent() {
        if (getIntent().getBooleanExtra("useSwipeToDelete", false)) {
            initialSwipeToDelete();
        }

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

        shuffle_play_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(LOG_TAG, "shuffle tracks: " + tracks.toString());
                if (trackList != null) {
                    MediaPlaybackService.addShuffleTrack(trackList);
                }
            }
        });
    }

    private void initialSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(trackAdapter);
        swipeToDeleteCallback.setOnSwipedListener(new SwipeToDeleteCallback.OnSwipedListener() {
            @Override
            public void onSwipedListener(int position) {
                //position: position of track swiped
                mRecentlyDeletedTrackPosition = position;
                mRecentlyDeletedTrack = trackList.get(position);
                mRecentlyDeletedIdPosition = trackList.get(position).getId();

                String trackName = trackList.get(position).getTitle();
                String playlistName = currentPlaylist.getTitle();

//                Log.d("testdelete", "playlistName: " + playlistName
//                        + ", trackName: " + trackName
//                        + ", position: " + position + ", size: " + trackList.size());

                trackList.remove(position);
                trackIds.remove(position);
                trackAdapter.update(trackList);
                PlaylistManager.getInstance().removeFromPlaylistTracks(playlistName, position);

                showUndoSnackbar(playlistName);
            }
        });

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(rvTracks);
    }

    private void showUndoSnackbar(final String trackName) {
        Snackbar snackbar = Snackbar.make(root_view, "Deleted \"" + trackName + "\"", Snackbar.LENGTH_LONG);
        snackbar.setAction("undo delete", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackList.add(mRecentlyDeletedTrackPosition, mRecentlyDeletedTrack);
                trackIds.add(mRecentlyDeletedTrackPosition, mRecentlyDeletedIdPosition);
                PlaylistManager.getInstance().setPlaylistTracks(trackName, trackIds);
                trackAdapter.update(trackList);
            }
        });
        snackbar.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(ShowMiniplayerEvent action) {
        miniPlayer_frame.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateDownloadedTrack updateDownloadedTrack) {
        trackAdapter.update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateFavouriteTrack updateFavouriteTrack) {
        trackAdapter.update();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
        trackAdapter.update();
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
        EventBus.getDefault().register(this);
        mediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback);
        mediaBrowser.disconnect();
    }
}
