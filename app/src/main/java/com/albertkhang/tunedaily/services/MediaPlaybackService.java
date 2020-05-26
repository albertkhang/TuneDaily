package com.albertkhang.tunedaily.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.MediaBrowserServiceCompat;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.broadcasts.BecomingNoisyReceiver;
import com.albertkhang.tunedaily.utils.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String LOG_TAG = "MediaPlaybackService";

    private static MediaSessionCompat mediaSession;
    private MediaSessionCompat.Callback mediaSessionCallback;
    private PlaybackStateCompat.Builder playbackStateBuilder;

    public static ArrayList<Track> tracks = new ArrayList<>();
    private static MediaPlayer player = new MediaPlayer();
    private Handler handler = new Handler();
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private AudioFocusRequest audioFocusRequest;
    private Runnable delayedStopRunnable;
    private BecomingNoisyReceiver becomingNoisyReceiver;
    private IntentFilter becomingNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private static int currentTrackPosition;

    @Override
    public void onCreate() {
        super.onCreate();

        initialBecomingNoisyReceiver();
        initialSession();

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    private void initialBecomingNoisyReceiver() {
        becomingNoisyReceiver = new BecomingNoisyReceiver();
        becomingNoisyReceiver.setOnReceiveListener(new BecomingNoisyReceiver.OnReceiveListener() {
            @Override
            public void onReceiveListener() {
                // Pause the playback
                mediaSession.getController().getTransportControls().pause();
            }
        });
    }

    private void initialAFChangeListener() {
//        initialDelayedStopRunnable();

        afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_LOSS");
                    // Permanent loss of audio focus
                    // Pause playback immediately
                    mediaSession.getController().getTransportControls().pause();

                    // Wait 30 seconds before stopping playback
//                    handler.postDelayed(delayedStopRunnable,
//                            TimeUnit.SECONDS.toMillis(30));
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_LOSS_TRANSIENT");
                    // Pause playback
                    mediaSession.getController().getTransportControls().pause();
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    // Lower the volume, keep playing
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_GAIN");
                    // Your app has been granted audio focus again
                    // Raise volume to normal, restart playback if necessary
                    mediaSession.getController().getTransportControls().play();
                }
            }
        };
    }

    private void initialDelayedStopRunnable() {
        delayedStopRunnable = new Runnable() {
            @Override
            public void run() {
                int state = mediaSession.getController().getPlaybackState().getState();
                if (state == PlaybackStateCompat.STATE_PAUSED) {
                    mediaSession.getController().getTransportControls().stop();
                }
            }
        };
    }

    public static void addTrack(Track track) {
        if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            player.stop();
            player.reset();
        }

        boolean isContain = false;

        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId() == track.getId()) {
                isContain = true;
                currentTrackPosition = i;
                break;
            }
        }

        if (!isContain) {
            Log.d(LOG_TAG, "not contains track " + track.toString());

            Track temp = new Track(track);
            tracks.add(temp);
//            Log.d(LOG_TAG, "tracks size: " + tracks.size() + " thread " + Thread.currentThread() + " " + player);

            try {
                player.setDataSource(tracks.get(tracks.size() - 1).getTrack());
                player.prepareAsync();
                player.start();
            } catch (IOException e) {
                Log.d(LOG_TAG, "e: " + e.toString());
            }
        } else {
            Log.d(LOG_TAG, "contains track " + track.toString());

            try {
                player.setDataSource(tracks.get(currentTrackPosition).getTrack());
                player.prepareAsync();
                player.start();
            } catch (IOException e) {
                Log.d(LOG_TAG, "e: " + e.toString());
            }
        }
    }

    private void initialSession() {
        Log.d(LOG_TAG, "initialSession");

        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, LOG_TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        playbackStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mediaSession.setPlaybackState(playbackStateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        initialAFChangeListener();
        initialSessionCallback();
        mediaSession.setCallback(mediaSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());
    }

    private void initialSessionCallback() {
        Log.d(LOG_TAG, "initialCallback");

        mediaSessionCallback = new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.d(LOG_TAG, "onPlay");

                //Audio Focus
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                // Request audio focus for playback, this registers the afChangeListener
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Log.d(LOG_TAG, "run");
                    audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setOnAudioFocusChangeListener(afChangeListener)
                            .setAudioAttributes(attrs)
                            .build();
                    int result = am.requestAudioFocus(audioFocusRequest);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        Log.d(LOG_TAG, "AUDIOFOCUS_REQUEST_GRANTED");

                        //Service
                        startService(new Intent(getApplicationContext(), MediaPlaybackService.class));

                        //Media Session
                        mediaSession.setActive(true);
                        //Update metadata and state
                        playbackStateBuilder = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f);
                        mediaSession.setPlaybackState(playbackStateBuilder.build());

                        //Player Implementation
                        player.start();

                        //Becoming Noisy
                        registerReceiver(becomingNoisyReceiver, becomingNoisyFilter);

                        //Notifications
//                        service.startForeground(id, myPlayerNotification);
                    }
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d(LOG_TAG, "onPause");

                //Media Session
                //Update metadata and state
                playbackStateBuilder = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1f);
                mediaSession.setPlaybackState(playbackStateBuilder.build());

                //Player Implementation
                player.pause();

                //Becoming Noisy
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(becomingNoisyReceiver);

                //Notifications

            }

            @Override
            public void onStop() {
                super.onStop();
                Log.d(LOG_TAG, "onStop");

                //Audio Focus
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    am.abandonAudioFocusRequest(audioFocusRequest);
                }

                //Service
                stopSelf();

                //Media Session
                mediaSession.setActive(false);
                //Update metadata and state
                playbackStateBuilder = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f);
                mediaSession.setPlaybackState(playbackStateBuilder.build());

                //Player Implementation
                player.stop();

                //Notifications

            }
        };
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(getString(R.string.app_name), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }
}
