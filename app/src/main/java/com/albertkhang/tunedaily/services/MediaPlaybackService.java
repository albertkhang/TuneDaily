package com.albertkhang.tunedaily.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.broadcasts.BecomingNoisyReceiver;
import com.albertkhang.tunedaily.events.ShowMiniplayerEvent;
import com.albertkhang.tunedaily.events.UpdateCurrentTrackStateEvent;
import com.albertkhang.tunedaily.events.UpdateTitleArtistEvent;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.networks.CheckFileSize;
import com.albertkhang.tunedaily.utils.DownloadTrackManager;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String LOG_TAG = "MediaPlaybackService";
    private static final String CHANNEL_ID = "com.albertkhang.tunedaily.channelidplaybacknotification";
    private static final int NOTIFICATION_ID = 299;
    private static Context context;

    private interface ACTION {
        String PLAY = "com.albertkhang.tunedaily.mediaplaybackservice.play";
        String PAUSE = "com.albertkhang.tunedaily.mediaplaybackservice.pause";
        String SKIP_TO_PREVIOUS = "com.albertkhang.tunedaily.mediaplaybackservice.skiptoprevious";
        String SKIP_TO_NEXT = "com.albertkhang.tunedaily.mediaplaybackservice.skiptonext";
        String CLOSE = "com.albertkhang.tunedaily.mediaplaybackservice.close";
    }

    public interface REPEAT {
        int NOT_REPEAT = 0;
        int REPEAT_ALL = 1;
        int REPEAT_ONE = 2;
    }

    private static int currentRepeat = 0;

    public static int getCurrentRepeat() {
        return currentRepeat;
    }

    public static void setCurrentRepeat(int state) {
        currentRepeat = state % 3;
    }

    private static MediaSessionCompat mediaSession;
    private MediaSessionCompat.Callback mediaSessionCallback;
    private PlaybackStateCompat.Builder playbackStateBuilder;

    public static ArrayList<Track> tracks = new ArrayList<>();
    private static MediaPlayer player;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private AudioFocusRequest audioFocusRequest;
    private BecomingNoisyReceiver becomingNoisyReceiver;
    private IntentFilter becomingNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NotificationCompat.Builder builder;

    private static int currentTrackPosition = -1;

    private static int lastPressSkipToPrevious = 0;
    private static final int SKIP_TO_PREVIOUS_INTERVAL = 10000;

    private static Track mRecentlyDeletedItem;
    private static int mRecentlyDeletedItemPosition;

    public static ArrayList<Track> getCurrentPlaylist() {
        return tracks;
    }

    public static int removeFromCurrentPlaylist(int position) {
        mRecentlyDeletedItemPosition = position;
        mRecentlyDeletedItem = getCurrentPlaylist().get(position);

        if (currentTrackPosition > position) {
            currentTrackPosition--;
            tracks.remove(position);
            return 1;//currentTrackPosition -1
        }

        if (currentTrackPosition < position) {
            tracks.remove(position);
            return 0;//No change currentTrackPosition
        }

        return -1;//equal
    }

    public static void undoDeleteFromCurrentPlaylist() {
        tracks.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);

        if (currentTrackPosition >= mRecentlyDeletedItemPosition) {
            currentTrackPosition++;
        }
    }

    public static int getCurrentPositionPlayer() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public static void setCurrentPositionPlayer(int mSec) {
//        Log.d(LOG_TAG, "mSec: " + mSec + ", player: " + player.getDuration());

        player.seekTo(mSec);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initialBecomingNoisyReceiver();
        initialNotificationChannelId();
        initialSession();

        context = getApplicationContext();
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
//                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_LOSS");
                    // Permanent loss of audio focus
                    // Pause playback immediately
                    mediaSession.getController().getTransportControls().pause();

                    // Wait 30 seconds before stopping playback
//                    handler.postDelayed(delayedStopRunnable,
//                            TimeUnit.SECONDS.toMillis(30));
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
//                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_LOSS_TRANSIENT");
                    // Pause playback
                    mediaSession.getController().getTransportControls().pause();
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    // Lower the volume, keep playing
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                    Log.d(LOG_TAG, "afChangeListener AUDIOFOCUS_GAIN");
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
//            Log.d(LOG_TAG, "not contains track " + track.toString());

            tracks.add(track);
            currentTrackPosition = tracks.size() - 1;
        }

        initialPlayer();

        try {
            //Handle load track
            if (DownloadTrackManager.isFileExists(tracks.get(currentTrackPosition))) {
                CheckFileSize checkFileSize = new CheckFileSize(context);
                checkFileSize.setOnPostExecuteListener(new CheckFileSize.OnPostExecuteListener() {
                    @Override
                    public void onPostExecuteListener(boolean isSameSize) {
                        if (isSameSize) {
                            Log.d(LOG_TAG, "play downloaded");

                            File file = DownloadTrackManager.getFile(tracks.get(currentTrackPosition));
                            try {
                                player.setDataSource(file.getPath());
                                player.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(LOG_TAG, "play online");
                            try {
                                player.setDataSource(tracks.get(currentTrackPosition).getTrack());
                                player.prepareAsync();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                checkFileSize.execute(track);
            } else {
                Log.d(LOG_TAG, "play online");
                player.setDataSource(tracks.get(currentTrackPosition).getTrack());
                player.prepareAsync();
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "e: " + e.toString());
        }

        mediaSession.getController().getTransportControls().play();

        //set metadata
        addMetadata(tracks.get(currentTrackPosition));
    }

    public static void addShuffleTrack(ArrayList<Track> shuffleTracks) {
        tracks.clear();

        int max = shuffleTracks.size() - 1;
        int min = 0;

        int x;
        for (int i = 0; i < shuffleTracks.size(); i++) {
            //select a random number different with i
            x = (int) ((Math.random() * ((max - min) + 1)) + min);

            Track tmpI = shuffleTracks.get(i);
            Track tmpX = shuffleTracks.get(x);

            shuffleTracks.set(i, tmpX);
            shuffleTracks.set(x, tmpI);
        }

        tracks.addAll(shuffleTracks);
        currentTrackPosition = 0;

        addTrack(tracks.get(0));
        EventBus.getDefault().post(new ShowMiniplayerEvent());
    }

    public interface OnPlayerCompletionListener {
        void onPlayerCompletionListener();
    }

    private static OnPlayerCompletionListener onPlayerCompletionListener;

    public static void setOnPlayerCompletionListener(OnPlayerCompletionListener onPlayerCompletionListener) {
        MediaPlaybackService.onPlayerCompletionListener = onPlayerCompletionListener;
    }

    private static void initialPlayer() {
        if (player != null) {
            player = null;
        }

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switch (currentRepeat) {
                    case REPEAT.NOT_REPEAT:
                        Log.d(LOG_TAG, "NOT_REPEAT");
                        if (currentTrackPosition == tracks.size() - 1) {
                            mediaSession.getController().getTransportControls().pause();
                        } else {
                            mediaSession.getController().getTransportControls().skipToNext();
                        }
                        resetPlayer();

                        break;

                    case REPEAT.REPEAT_ALL:
                        Log.d(LOG_TAG, "REPEAT_ALL");
                        mediaSession.getController().getTransportControls().skipToNext();
                        break;

                    case REPEAT.REPEAT_ONE:
                        Log.d(LOG_TAG, "REPEAT_ONE");
                        player.stop();
                        player.prepareAsync();
                        player.start();
                        resetPlayer();
                        break;
                }

                if (onPlayerCompletionListener != null) {
                    onPlayerCompletionListener.onPlayerCompletionListener();
                }
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
    }

    private static void resetPlayer() {
        player.seekTo(1);
    }

    private static void addMetadata(Track track) {
        MediaMetadataCompat meta = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getCover())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, track.getCover())
                .build();
        mediaSession.setMetadata(meta);
    }

    private void initialSession() {
//        Log.d(LOG_TAG, "initialSession");

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

    @SuppressLint("CheckResult")
    private void initialPlaybackNotification() {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

//        Log.d(LOG_TAG, "title: " + description.getTitle());
//        Log.d(LOG_TAG, "subtitle: " + description.getSubtitle());
//        Log.d(LOG_TAG, "description: " + description.getDescription());
//        Log.d(LOG_TAG, "cover: " + description.getIconUri());

        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        final Bitmap[] bitmap = new Bitmap[1];

        Glide.with(this)
                .asBitmap()
                .load(description.getIconUri())
                .placeholder(R.drawable.ic_playlist_cover)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap[0] = resource;
//                        Log.d(LOG_TAG, "onResourceReady");
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                        Log.d(LOG_TAG, "onLoadCleared");
                    }
                });

        builder
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(bitmap[0])

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
                .setShowWhen(true);

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
//        Log.d(LOG_TAG, "initialCallback");

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
//                    Log.d(LOG_TAG, "run");
                    audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setOnAudioFocusChangeListener(afChangeListener)
                            .setAudioAttributes(attrs)
                            .build();
                    int result = am.requestAudioFocus(audioFocusRequest);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                        Log.d(LOG_TAG, "AUDIOFOCUS_REQUEST_GRANTED");

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
            public void onRewind() {
                super.onRewind();
                player.seekTo(0);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                if (!isOneItemInList()) {
                    currentTrackPosition++;
                    currentTrackPosition = currentTrackPosition % tracks.size();
                    Log.d(LOG_TAG, "onSkipToNext position: " + currentTrackPosition);

                    Track track = tracks.get(currentTrackPosition);
                    addTrack(track);
                    EventBus.getDefault().post(new UpdateTitleArtistEvent(track.getTitle(), track.getArtist()));
                    EventBus.getDefault().post(new UpdateCurrentTrackStateEvent(track));
                } else {
                    mediaSession.getController().getTransportControls().pause();
                    if (onPlayerCompletionListener != null) {
                        onPlayerCompletionListener.onPlayerCompletionListener();
                    }
                }
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                if (player.getCurrentPosition() - lastPressSkipToPrevious >= SKIP_TO_PREVIOUS_INTERVAL) {
                    player.seekTo(0);
                } else {
                    if (!isOneItemInList()) {
                        currentTrackPosition--;
                        currentTrackPosition = currentTrackPosition + tracks.size();
                        currentTrackPosition = currentTrackPosition % tracks.size();
                        Log.d(LOG_TAG, "onSkipToPrevious position: " + currentTrackPosition);

                        Track track = tracks.get(currentTrackPosition);
                        addTrack(track);
                        EventBus.getDefault().post(new UpdateTitleArtistEvent(track.getTitle(), track.getArtist()));
                        EventBus.getDefault().post(new UpdateCurrentTrackStateEvent(track));
                    }
                }
            }
        };
    }

    private boolean isOneItemInList() {
        if (tracks.size() == 1) {
            Toast.makeText(this, "You have only 1 song in queue.", Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
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
