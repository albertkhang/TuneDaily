package com.albertkhang.tunedaily.fragments;

import android.content.ComponentName;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.animations.RotateAnimation;
import com.albertkhang.tunedaily.events.UpdateCurrentTrackEvent;
import com.albertkhang.tunedaily.events.UpdateCurrentTrackStateEvent;
import com.albertkhang.tunedaily.events.UpdateFavouriteTrack;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.TimeConverter;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.views.CircleImageView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.Objects;

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

    private RotateAnimation rotateAnimation;
    private Runnable runnable;

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

    private Handler handler;

    private void startUpdatingPlayerPosition() {
        stopUpdatingPlayerPosition();
        runnable.run();
    }

    private void stopUpdatingPlayerPosition() {
        handler.removeCallbacks(runnable);
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

        rotateAnimation = new RotateAnimation(imgCover);
        handler = new Handler();
        initialUpdatingSeekBarRunnable();

        initialControllerCallback();
        initialConnectionCallback();
        initialMediaBrowser();

        MediaPlaybackService.setOnPlayerCompletionListener(new MediaPlaybackService.OnPlayerCompletionListener() {
            @Override
            public void onPlayerCompletionListener() {
                seekbar.setProgress(0);
                txtTimeStampStart.setText(TimeConverter.getInstance().getTimestamp(0));
            }
        });

        updateTheme();
    }

    private void initialUpdatingSeekBarRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    seekbar.setProgress(MediaPlaybackService.getCurrentPositionPlayer() / 1000);
                    handler.postDelayed(this, 1000);
                } else {
                    stopUpdatingPlayerPosition();
                }
            }
        };
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

                    rotateAnimation.stop();
                } else {
                    Log.d(LOG_TAG, "STATE_PAUSED");
                    mediaController.getTransportControls().play();
                    imgPlayPause.setImageResource(R.drawable.ic_pause);

                    rotateAnimation.start();
                }
            }
        });

        // Display the initial state
        Log.d(LOG_TAG, "Display the initial state");
        updateMetadata(mediaController.getMetadata());
        updatePlaybackState(mediaController.getPlaybackState());
        currentTrack = MediaPlaybackService.getCurrentTrack();
        updateFavourite();
        updateRotateAnimation();
        seekbar.setMax(currentTrack.getDuration());

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    private void updateRotateAnimation() {
        if (mediaController != null) {
            int pbState = mediaController.getPlaybackState().getState();
            if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                rotateAnimation.start();
            } else {
                rotateAnimation.stop();
            }
        }
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
            startUpdatingPlayerPosition();
            rotateAnimation.start();
        } else {
            Log.d(LOG_TAG, "controllerCallback STATE_PAUSED");
            imgPlayPause.setImageResource(R.drawable.ic_play);
            rotateAnimation.stop();
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

            Glide.with(requireActivity()).load(cover).placeholder(R.color.colorLight5).into(imgCover);
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

//            imgRepeat.setColorFilter(getResources().getColor(R.color.colorLight5));
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

//            imgRepeat.setColorFilter(getResources().getColor(R.color.colorDark5));
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

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.d(LOG_TAG, "progress: " + progress);
                txtTimeStampStart.setText(TimeConverter.getInstance().getTimestamp(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaController.getTransportControls().pause();
                stopUpdatingPlayerPosition();
                rotateAnimation.stop();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(LOG_TAG, "progress: " + seekBar.getProgress() + ", max: " + seekBar.getMax());

                MediaPlaybackService.setCurrentPositionPlayer(seekBar.getProgress() * 1000);
                mediaController.getTransportControls().play();
                startUpdatingPlayerPosition();
                rotateAnimation.start();
            }
        });

        imgSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "imgSkipNext");
                mediaController.getTransportControls().skipToNext();
            }
        });

        imgSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "imgSkipPrevious");
                mediaController.getTransportControls().skipToPrevious();
            }
        });

        imgRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentRepeat = MediaPlaybackService.getCurrentRepeat();
                currentRepeat++;

                MediaPlaybackService.setCurrentRepeat(currentRepeat);
                updateRepeatState(MediaPlaybackService.getCurrentRepeat());
            }
        });
    }

    private void updateRepeatState(int currentRepeat) {
        switch (currentRepeat) {
            case MediaPlaybackService.REPEAT.NOT_REPEAT:
                imgRepeat.setImageResource(R.drawable.ic_not_repeat);
                imgRepeat.setColorFilter(getResources().getColor(R.color.colorLight5));
                break;

            case MediaPlaybackService.REPEAT.REPEAT_ALL:
                imgRepeat.setImageResource(R.drawable.ic_repeat_all);
                imgRepeat.setColorFilter(getResources().getColor(R.color.colorLight1));
                break;

            case MediaPlaybackService.REPEAT.REPEAT_ONE:
                imgRepeat.setImageResource(R.drawable.ic_repeat_1);
                imgRepeat.setColorFilter(getResources().getColor(R.color.colorLight1));
                break;
        }
    }

    private void updateFavouriteState() {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveTrack(UpdateCurrentTrackEvent updateCurrentTrackEvent) {
        currentTrack = updateCurrentTrackEvent.getTrack();

        seekbar.setMax(currentTrack.getDuration());
        txtTimeStampEnd.setText(TimeConverter.getInstance().getTimestamp(currentTrack.getDuration()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveTrack(UpdateFavouriteTrack updateFavouriteTrack) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveTrack(UpdateCurrentTrackStateEvent updateCurrentTrackStateEvent) {
        Track track = updateCurrentTrackStateEvent.getTrack();

        seekbar.setMax(track.getDuration());
        txtTimeStampEnd.setText(TimeConverter.getInstance().getTimestamp(track.getDuration()));
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
        updateRotateAnimation();
        seekbar.setProgress(MediaPlaybackService.getCurrentPositionPlayer() / 1000);
        updateFavouriteState();
        updateRepeatState(MediaPlaybackService.getCurrentRepeat());
    }

    @Override
    public void onPause() {
        super.onPause();
        rotateAnimation.stop();
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

