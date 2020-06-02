package com.albertkhang.tunedaily.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.FullPlayerActivity;
import com.albertkhang.tunedaily.services.MediaPlaybackConnectHelper;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.bumptech.glide.Glide;
import com.google.common.eventbus.AllowConcurrentEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

public class MiniPlayerFragment extends Fragment implements Serializable {
    private static final String LOG_TAG = "MiniPlayerFragment";

    public interface ACTION {
        String PLAY = "com.albertkhang.action.play";
    }

    private ConstraintLayout miniPlayer_background;
    private SettingManager settingManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

    private boolean isFavourite = false;

    private ImageView imgCover;
    private ImageView imgFavourite;
    private ImageView imgPlayPause;
    private ImageView imgSkipNext;
    private TextView txtTitle;
    private TextView txtArtist;
    private ConstraintLayout root_view;

    private Track currentTrack;

    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback connectionCallback;
    private MediaControllerCompat.Callback controllerCallback;
    private MediaControllerCompat mediaController;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    private void addControl(View view) {
        imgCover = view.findViewById(R.id.imgCover);
        imgFavourite = view.findViewById(R.id.imgFavourite);
        imgPlayPause = view.findViewById(R.id.imgPlayPause);
        imgSkipNext = view.findViewById(R.id.imgSkipNext);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtArtist = view.findViewById(R.id.txtArtist);
        root_view = view.findViewById(R.id.root_view);

        miniPlayer_background = view.findViewById(R.id.miniPlayer_background);
        settingManager = SettingManager.getInstance(getContext());

        mediaController = MediaControllerCompat.getMediaController(getActivity());

        initialControllerCallback();
        initialConnectionCallback();
        initialMediaBrowser();

        updateIfIsPlaying();

        Log.d(LOG_TAG, "addControl");
    }

    private void updateIfIsPlaying() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getBoolean("isPlaying")) {
            Track track = MediaPlaybackConnectHelper.getCurrentTrack();

            txtTitle.setText(track.getTitle());
            txtArtist.setText(track.getArtist());
            imgPlayPause.setImageResource(R.drawable.ic_pause);
            Glide.with(this).load(track.getCover()).into(imgCover);
        }
    }

    private void initialControllerCallback() {
        controllerCallback = new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                super.onMetadataChanged(metadata);
                Log.d(LOG_TAG, "metadata: " + metadata.toString());
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
                Log.d(LOG_TAG, "state: " + state.toString());

                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    Log.d(LOG_TAG, "controllerCallback STATE_PLAYING");
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                } else {
                    Log.d(LOG_TAG, "controllerCallback STATE_PAUSED");
                    imgPlayPause.setImageResource(R.drawable.ic_play);
                }
            }
        };
    }

    private void initialConnectionCallback() {
        connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                super.onConnected();
                Log.d(LOG_TAG, "onConnected");

                // Get the token for the MediaSession
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                Log.d(LOG_TAG, "token: " + token.toString());

                // Create a MediaControllerCompat
                try {
                    mediaController =
                            new MediaControllerCompat(getContext(), // Context
                                    token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(getActivity(), mediaController);

                    // Finish building the UI
                    buildTransportControls();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void buildTransportControls() {
        imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pbState = mediaController.getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    Log.d(LOG_TAG, "STATE_PLAYING");
                    mediaController.getTransportControls().pause();
                } else {
                    Log.d(LOG_TAG, "STATE_PAUSED");
                    mediaController.getTransportControls().play();
                }
            }
        });

        // Display the initial state
        mediaController.getMetadata();
        mediaController.getPlaybackState();

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    private void initialMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(getContext(),
                new ComponentName(requireContext(), MediaPlaybackService.class),
                connectionCallback,
                null); // optional Bundle
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

        root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FullPlayerActivity.class);
                intent.putExtra("current_track", currentTrack);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
        updateCurrentTrack();
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        updateCurrentStatus();
    }

    private void updateCurrentStatus() {
        if (mediaController != null) {
            int pbState = mediaController.getPlaybackState().getState();
            if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                Log.d(LOG_TAG, "updateCurrentStatus STATE_PLAYING");
                imgPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                Log.d(LOG_TAG, "updateCurrentStatus STATE_PAUSED");
                imgPlayPause.setImageResource(R.drawable.ic_play);
            }
        }
    }

    private void updateCurrentTrack() {
        if (currentTrack != null) {
            setCover(currentTrack.getCover());
            txtTitle.setText(currentTrack.getTitle());
            txtArtist.setText(currentTrack.getArtist());
        }
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            setMiniPlayerBackground(R.color.colorDark2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorLight1));

            imgCover.setImageResource(R.color.colorDark5);
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
        } else {
            setMiniPlayerBackground(R.color.colorLight2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorDark2));

            imgCover.setImageResource(R.color.colorLight5);
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
        }
    }

    private void setMiniPlayerBackground(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.round_corner_top_background);
        drawable.setTint(getResources().getColor(color));
        miniPlayer_background.setBackground(drawable);
    }

    private void setCover(String cover) {
        Glide.with(this).load(cover).placeholder(R.color.colorLight5).into(imgCover);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(Track track) {
        currentTrack = new Track(track);
        updateCurrentTrack();

        MediaPlaybackService.addTrack(currentTrack);

        mediaController.getTransportControls().prepare();
        mediaController.getTransportControls().play();
        Log.d(LOG_TAG, "play track: " + track.toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }
}
