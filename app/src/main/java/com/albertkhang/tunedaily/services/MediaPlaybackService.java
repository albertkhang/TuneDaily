package com.albertkhang.tunedaily.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.broadcasts.BecomingNoisyReceiver;
import com.albertkhang.tunedaily.utils.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String LOG_TAG = "MediaPlaybackService";
    private static final String CHANNEL_ID = "com.albertkhang.tunedaily.channelidplaybacknotification";
    private static final int NOTIFICATION_ID = 299;

    private interface ACTION {
        String PLAY = "com.albertkhang.tunedaily.mediaplaybackservice.play";
        String PAUSE = "com.albertkhang.tunedaily.mediaplaybackservice.pause";
        String SKIP_TO_PREVIOUS = "com.albertkhang.tunedaily.mediaplaybackservice.skiptoprevious";
        String SKIP_TO_NEXT = "com.albertkhang.tunedaily.mediaplaybackservice.skiptonext";
        String CLOSE = "com.albertkhang.tunedaily.mediaplaybackservice.close";
    }

    private static MediaSessionCompat mediaSession;
    private MediaSessionCompat.Callback mediaSessionCallback;
    private PlaybackStateCompat.Builder playbackStateBuilder;

    public static ArrayList<Track> tracks = new ArrayList<>();
    private static MediaPlayer player = new MediaPlayer();
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private AudioFocusRequest audioFocusRequest;
    private BecomingNoisyReceiver becomingNoisyReceiver;
    private IntentFilter becomingNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NotificationCompat.Builder builder;

    private static int currentTrackPosition = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        initialBecomingNoisyReceiver();
        initialNotificationChannelId();
        initialSession();

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    public static Track getCurrentTrack() {
        if (currentTrackPosition != -1) {
            return tracks.get(currentTrackPosition);
        }
        return null;
    }

    private void initialNotificationChannelId() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(false);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
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

            currentTrackPosition = tracks.size() - 1;
        }

        try {
            player.setDataSource(tracks.get(currentTrackPosition).getTrack());
        } catch (IOException e) {
            Log.d(LOG_TAG, "e: " + e.toString());
        }

        //set metadata
        addMetadata(tracks.get(currentTrackPosition));
    }

    private static void addMetadata(Track track) {
        MediaMetadataCompat meta = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getCover())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist())
                .build();
        mediaSession.setMetadata(meta);
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
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1f);

        mediaSession.setPlaybackState(playbackStateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        initialAFChangeListener();
        initialSessionCallback();
        mediaSession.setCallback(mediaSessionCallback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());
    }

    private void initialPlaybackNotification() {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        Log.d(LOG_TAG, "title: " + description.getTitle());
        Log.d(LOG_TAG, "subtitle: " + description.getSubtitle());
        Log.d(LOG_TAG, "description: " + description.getDescription());

        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cover);

        builder
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(bitmap)

                // Enable launching the player by clicking the notification
                .setContentIntent(controller.getSessionActivity())

                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))

                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                .setSmallIcon(R.drawable.ic_play)

                // Take advantage of MediaStyle features
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2, 3)

                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP)))
                .setShowWhen(false);

        initialPlaybackAction(controller);
    }

    private void initialPlaybackAction(MediaControllerCompat controller) {
        NotificationCompat.Action aSkipToPrevious = new NotificationCompat.Action(
                R.drawable.ic_skip_previous,
                "SKIP_TO_PREVIOUS",
                getPlaybackNotificationAction(ACTION.SKIP_TO_PREVIOUS)
        );
        builder.addAction(aSkipToPrevious);

        if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            NotificationCompat.Action aPause = new NotificationCompat.Action(
                    R.drawable.ic_pause,
                    "PAUSE",
                    getPlaybackNotificationAction(ACTION.PAUSE)
            );
            builder.addAction(aPause);
        } else {
            NotificationCompat.Action aPlay = new NotificationCompat.Action(
                    R.drawable.ic_play,
                    "PLAY",
                    getPlaybackNotificationAction(ACTION.PLAY)
            );
            builder.addAction(aPlay);
        }

        NotificationCompat.Action aSkipToNext = new NotificationCompat.Action(
                R.drawable.ic_skip_next,
                "SKIP_TO_NEXT",
                getPlaybackNotificationAction(ACTION.SKIP_TO_NEXT)
        );
        builder.addAction(aSkipToNext);

        NotificationCompat.Action aClose = new NotificationCompat.Action(
                R.drawable.ic_close,
                "CLOSE",
                getPlaybackNotificationAction(ACTION.CLOSE)
        );
        builder.addAction(aClose);
    }

    private PendingIntent getPlaybackNotificationAction(String action) {
        Intent intent = new Intent(this, MediaPlaybackService.class);
        switch (action) {
            case ACTION.SKIP_TO_PREVIOUS:
                intent.setAction(ACTION.SKIP_TO_PREVIOUS);
                return PendingIntent.getService(this, 0, intent, 0);

            case ACTION.PLAY:
                intent.setAction(ACTION.PLAY);
                return PendingIntent.getService(this, 0, intent, 0);

            case ACTION.PAUSE:
                intent.setAction(ACTION.PAUSE);
                return PendingIntent.getService(this, 0, intent, 0);

            case ACTION.SKIP_TO_NEXT:
                intent.setAction(ACTION.SKIP_TO_NEXT);
                return PendingIntent.getService(this, 0, intent, 0);

            case ACTION.CLOSE:
                intent.setAction(ACTION.CLOSE);
                return PendingIntent.getService(this, 0, intent, 0);

            default:
                return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            Log.d(LOG_TAG, "action: " + intent.getAction());
            switch (intent.getAction()) {
                case ACTION.SKIP_TO_PREVIOUS:
                    mediaSession.getController().getTransportControls().skipToPrevious();
                    break;

                case ACTION.PLAY:
                    mediaSession.getController().getTransportControls().play();
                    break;

                case ACTION.PAUSE:
                    mediaSession.getController().getTransportControls().pause();
                    break;

                case ACTION.SKIP_TO_NEXT:
                    mediaSession.getController().getTransportControls().skipToNext();
                    break;

                case ACTION.CLOSE:
                    closePlaybackNotification();
                    break;
            }
        }

        return Service.START_STICKY;
    }

    private void closePlaybackNotification() {
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
        player.pause();

        //Notifications
//                    initialPlaybackNotification();
        stopForeground(true);
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
                        initialPlaybackNotification();
                        startForeground(NOTIFICATION_ID, builder.build());
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
                initialPlaybackNotification();
                startForeground(NOTIFICATION_ID, builder.build());
                stopForeground(false);
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
                stopForeground(true);
            }

            @Override
            public void onPrepare() {
                super.onPrepare();
                player.prepareAsync();
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
