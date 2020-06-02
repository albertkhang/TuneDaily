package com.albertkhang.tunedaily.services;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.Track;

public class MediaPlaybackConnectHelper {
    private static final String LOG_TAG = "MediaPbConnectHelper";
    private Activity activity;

    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback connectionCallbacks;
    private MediaControllerCompat.Callback controllerCallback;
    private MediaControllerCompat mediaController;

    public MediaPlaybackConnectHelper(Activity activity) {
        this.activity = activity;

        initialControllerCallback();
        initialConnectionCallback();
        initialMediaBrowser();
    }

    public static Track getCurrentTrack() {
        return MediaPlaybackService.getCurrentTrack();
    }

    private void initialControllerCallback() {
        controllerCallback =
                new MediaControllerCompat.Callback() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        //TODO: Add onMetadataChanged callback
                    }

                    @Override
                    public void onPlaybackStateChanged(PlaybackStateCompat state) {
                        //TODO: Add onPlaybackStateChanged callback
                    }
                };
    }

    public interface OnPlayingListener {
        void onPlayingListener(boolean isPlaying);
    }

    private OnPlayingListener onPlayingListener;

    public void setOnPlayingListener(OnPlayingListener onPlayingListener) {
        this.onPlayingListener = onPlayingListener;
    }

    private void initialConnectionCallback() {
        connectionCallbacks = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d(LOG_TAG, "onConnected");

                // Get the token for the MediaSession
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                // Create a MediaControllerCompat
                try {
                    mediaController = new MediaControllerCompat(activity, token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(activity, mediaController);

                    // Finish building the UI
                    buildTransportControls();

                    Log.d(LOG_TAG, "state: " + mediaController.getPlaybackState().getState());
                    if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        //TODO: Add isPlaying State
                        Log.d(LOG_TAG, "STATE_PLAYING");
                        onPlayingListener.onPlayingListener(true);
                    } else {
                        onPlayingListener.onPlayingListener(false);
                    }
                } catch (RemoteException e) {
                    Log.d(LOG_TAG, "connectionCallbacks e: " + e.toString());
                }
            }

            @Override
            public void onConnectionSuspended() {
                // The Service has crashed. Disable transport controls until it automatically reconnects
                Log.d(LOG_TAG, "onConnectionSuspended");
            }

            @Override
            public void onConnectionFailed() {
                // The Service has refused our connection
                Log.d(LOG_TAG, "onConnectionFailed");
            }
        };
    }

    private void buildTransportControls() {
        //TODO: Play/Pause button

        // Grab the view for the play/pause button
//        playPause = (ImageView) findViewById(R.id.play_pause);

        // Attach a listener to the button
//        playPause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Since this is a play/pause button, you'll need to test the current state
//                // and choose the action accordingly
//
//                int pbState = mediaController.getPlaybackState().getState();
//                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
//                    mediaController.getTransportControls().pause();
//                } else {
//                    mediaController.getTransportControls().play();
//                }
//            }
//        });

        mediaController = MediaControllerCompat.getMediaController(activity);

        //TODO: Display the initial state
//        MediaMetadataCompat metadata = mediaController.getMetadata();
//        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);

    }

    private void initialMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(activity,
                new ComponentName(activity, MediaPlaybackService.class),
                connectionCallbacks,
                null); // optional Bundle
    }

    //Put in onStart()
    public void putInOnStart() {
        mediaBrowser.connect();
    }

    //Put in onResume()
    public void putInOnResume() {
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void putInOnStop() {
        // (see "stay in sync with the MediaSession")
        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
        }

        mediaBrowser.disconnect();
    }
}
