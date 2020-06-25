package com.albertkhang.tunedaily.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

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
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.FullPlayerActivity;
import com.albertkhang.tunedaily.events.UpdateCurrentTrackEvent;
import com.albertkhang.tunedaily.events.UpdateFavouriteTrack;
import com.albertkhang.tunedaily.events.UpdateTitleArtistEvent;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.models.Track;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

public class MiniPlayerFragment extends Fragment implements Serializable {
    private static final String LOG_TAG = "MiniPlayerFragment";

    private ConstraintLayout miniPlayer_background;
    private SettingManager settingManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

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

    public static boolean isOpenedFullPlayer = false;

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

            if (getActivity() != null) {
                Glide.with(getActivity()).load(cover).placeholder(R.color.colorLight5).into(imgCover);
                txtTitle.setText(title);
                txtArtist.setText(artist);

                EventBus.getDefault().post(new UpdateTitleArtistEvent(title, artist));
            }
        }
    }

    private void updateFavouriteState() {
        imgFavourite.setImageResource(R.drawable.ic_not_favourite);
        if (settingManager.isDarkTheme()) {
            imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight5));
        } else {
            imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark5));
        }
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

                if (PlaylistManager.getInstance().isContainInLikedSongs(currentTrack.getId())) {
                    PlaylistManager.getInstance().removeFromLikedSongs(currentTrack.getId());
                    imgFavourite.setImageResource(R.drawable.ic_not_favourite);
                    if (settingManager.isDarkTheme()) {
                        imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight5));
                    } else {
                        imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark5));
                    }
                    Toast.makeText(getContext(), "Removed \"" + currentTrack.getTitle() + "\" from liked songs", Toast.LENGTH_LONG).show();
                } else {
                    PlaylistManager.getInstance().addToLikedSongs(currentTrack.getId());
                    imgFavourite.setColorFilter(getResources().getColor(R.color.colorMain3));
                    imgFavourite.setImageResource(R.drawable.ic_favourite_blue);
                    Toast.makeText(getContext(), "Added \"" + currentTrack.getTitle() + "\" in liked songs", Toast.LENGTH_LONG).show();
                }

                EventBus.getDefault().post(new UpdateFavouriteTrack(currentTrack.getId()));
            }
        });

        root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOpenedFullPlayer) {
                    Intent intent = new Intent(getActivity(), FullPlayerActivity.class);
                    startActivity(intent);
                }
            }
        });

        imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pbState = mediaController.getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
//                    Log.d(LOG_TAG, "STATE_PLAYING");
                    mediaController.getTransportControls().pause();
                    imgPlayPause.setImageResource(R.drawable.ic_play);
                } else {
//                    Log.d(LOG_TAG, "STATE_PAUSED");
                    mediaController.getTransportControls().play();
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        imgSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.getTransportControls().skipToNext();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            setMiniPlayerBackground(R.color.colorDark2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorLight4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorLight1));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorLight1));

            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
        } else {
            setMiniPlayerBackground(R.color.colorLight2);

            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtArtist.setTextColor(getResources().getColor(R.color.colorDark4));

            imgFavourite.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgPlayPause.setColorFilter(getResources().getColor(R.color.colorDark2));
            imgSkipNext.setColorFilter(getResources().getColor(R.color.colorDark2));

            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
        }
    }

    private void setMiniPlayerBackground(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.round_corner_top_background);
        drawable.setTint(getResources().getColor(color));
        miniPlayer_background.setBackground(drawable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateCurrentTrackEvent updateCurrentTrackEvent) {
        Log.d(LOG_TAG, "onPlayAction: " + updateCurrentTrackEvent.getTrack().toString());
        MediaPlaybackService.addTrack(updateCurrentTrackEvent.getTrack());
        currentTrack = updateCurrentTrackEvent.getTrack();

        updateMetadata(mediaController.getMetadata());
        updatePlaybackState(mediaController.getPlaybackState());
        updateFavouriteState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateFavouriteTrack updateFavouriteTrack) {
        if (updateFavouriteTrack.getId() == currentTrack.getId()) {
            if (PlaylistManager.getInstance().isContainInLikedSongs(updateFavouriteTrack.getId())) {
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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mediaBrowser.connect();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
        Log.d(LOG_TAG, "onStop");
    }
}
