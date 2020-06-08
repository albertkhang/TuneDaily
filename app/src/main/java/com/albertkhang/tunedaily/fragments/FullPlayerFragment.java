package com.albertkhang.tunedaily.fragments;

import android.content.ComponentName;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.TimeConverter;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.views.CircleImageView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

public class FullPlayerFragment extends Fragment implements Serializable {
    private static final String LOG_TAG = "FullPlayerFragment";

    private SettingManager settingManager;
    private CircleImageView imgCover;
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

    private Track currentTrack;

    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback connectionCallback;
    private MediaControllerCompat.Callback controllerCallback;
    private MediaControllerCompat mediaController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_full_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        updateDataIntent();
        addEvent();
    }

    private void updateDataIntent() {
        currentTrack = MediaPlaybackService.getCurrentTrack();

        if (currentTrack != null) {
            txtTimeStampEnd.setText(TimeConverter.getInstance().getTimestamp(currentTrack.getDuration()));
        }
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());

        seekbar = view.findViewById(R.id.seekbar);
        imgCover = view.findViewById(R.id.imgCover);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtArtist = view.findViewById(R.id.txtArtist);
        txtTimeStampStart = view.findViewById(R.id.txtTimeStampStart);
        txtTimeStampEnd = view.findViewById(R.id.txtTimeStampEnd);

        imgSkipPrevious = view.findViewById(R.id.imgSkipPrevious);
        imgPlayPause = view.findViewById(R.id.imgPlayPause);
        imgSkipNext = view.findViewById(R.id.imgSkipNext);
        imgFavourite = view.findViewById(R.id.imgFavourite);
        imgRepeat = view.findViewById(R.id.imgRepeat);

        initialControllerCallback();
        initialConnectionCallback();
        initialMediaBrowser();

        updateTheme();
    }

    private void initialMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(getContext(),
                new ComponentName(requireContext(), MediaPlaybackService.class),
                connectionCallback,
                null); // optional Bundle
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
                    imgPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    Log.d(LOG_TAG, "STATE_PAUSED");
                    mediaController.getTransportControls().play();
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        // Display the initial state
        Log.d(LOG_TAG, "Display the initial state");
        updateMetadata(mediaController.getMetadata());
        updatePlaybackState(mediaController.getPlaybackState());
        currentTrack = MediaPlaybackService.getCurrentTrack();
        updateFavourite();

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }
    
    private void updateFavourite() {
        if (currentTrack != null) {
            if (PlaylistManager.getInstance().isContainInLikedSongs(currentTrack.getId())) {
                imgFavourite.setColorFilter(getResources().getColor(R.color.colorMain3));
                imgFavourite.setImageResource(R.drawable.ic_favourite_blue);
            } else {
                imgFavourite.setImageResource(R.drawable.ic_not_favourite);
                if (settingManager.isDarkTheme()) {
                    imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight5));
                } else {
                    imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark5));
                }
            }
        }
    }

    private void initialControllerCallback() {
        Log.d(LOG_TAG, "initialControllerCallback");
        controllerCallback = new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                super.onMetadataChanged(metadata);
                updateMetadata(metadata);
                currentTrack = MediaPlaybackService.getCurrentTrack();
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
                updatePlaybackState(state);
            }
        };
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            Log.d(LOG_TAG, "controllerCallback STATE_PLAYING");
            imgPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            Log.d(LOG_TAG, "controllerCallback STATE_PAUSED");
            imgPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        if (metadata != null) {
            String cover = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
            String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            Log.d(LOG_TAG, "cover: " + cover);
            Log.d(LOG_TAG, "title: " + title);
            Log.d(LOG_TAG, "artist: " + artist);

            Glide.with(getActivity()).load(cover).placeholder(R.color.colorLight5).into(imgCover);
            txtTitle.setText(title);
            txtArtist.setText(artist);
        }
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
        imgFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PlaylistManager.getInstance().isContainInLikedSongs(currentTrack.getId())) {
                    PlaylistManager.getInstance().removeFromLikedSongs(currentTrack.getId());
                    imgFavourite.setImageResource(R.drawable.ic_not_favourite);
                    if (settingManager.isDarkTheme()) {
                        imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight5));
                    } else {
                        imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark5));
                    }
                } else {
                    PlaylistManager.getInstance().addToLikedSongs(currentTrack.getId());
                    imgFavourite.setColorFilter(getResources().getColor(R.color.colorMain3));
                    imgFavourite.setImageResource(R.drawable.ic_favourite_blue);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveTrack(Track track) {
        Glide.with(this).load(track.getCover()).into(imgCover);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mediaBrowser.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
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

