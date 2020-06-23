package com.albertkhang.tunedaily.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.ViewPagerAdapter;
import com.albertkhang.tunedaily.events.ShowMiniplayerEvent;
import com.albertkhang.tunedaily.events.UpdateLanguageEvent;
import com.albertkhang.tunedaily.fragments.DiscoverFragment;
import com.albertkhang.tunedaily.fragments.LibraryFragment;
import com.albertkhang.tunedaily.fragments.MiniPlayerFragment;
import com.albertkhang.tunedaily.fragments.SearchFragment;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.events.UpdateThemeEvent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ConstraintLayout splash_screen;
    private static final int SHOWING_INTERVAL = 2000;
    public static final String MINI_PLAYER_TAG = "com.albertkhang.activities.mainactivity.miniplayer";
    public static final String LOG_TAG = "MainActivity";

    //This is our viewPager
    private ViewPager viewPager;
    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;

    //Fragments
    private DiscoverFragment discoverFragment;
    private SearchFragment searchFragment;
    private LibraryFragment libraryFragment;

    private MenuItem menuItem;

    private SettingManager settingManager;
    private FrameLayout bottom_gradient_frame;
    private FrameLayout miniPlayer_frame;

    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback connectionCallback;
    private MediaControllerCompat.Callback controllerCallback;
    private MediaControllerCompat mediaController;

    private NodePlayer nodePlayer;
    private static final String STREAMING_URL = "rtmp://45.76.150.28/live/android";

    private static boolean isJoinStream = true;

    private static long lastBackPress = 0;
    private static final long BACK_PRESS_INTERVAL = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControl();
        addEvent();

        //updateLanguage
        settingManager.updateLanguageConfiguration();
        EventBus.getDefault().post(new UpdateLanguageEvent());
    }

    private void initialNodePlayer() {
        nodePlayer = new NodePlayer(this);
        nodePlayer.setInputUrl(STREAMING_URL);
        nodePlayer.setAudioEnable(false);
        nodePlayer.setVideoEnable(false);
        nodePlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
            @Override
            public void onEventCallback(NodePlayer player, int event, String msg) {
                Log.d(LOG_TAG, "event: " + event + ", msg: " + msg);

                switch (event) {
                    case 1001://NetConnection.Connect.Success
                        if (isJoinStream) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showStreamDialog();
                                        }
                                    });
                                }
                            });
                        }

                        break;

                    case 1003://NetConnection.Connect.Reconnection
                        if (streamDialog != null) {
                            streamDialog.cancel();
                        }
                        break;
                }
            }
        });
    }

    private Dialog streamDialog;

    private void showStreamDialog() {
        streamDialog = new Dialog(this, R.style.RoundCornerDialogFragment);
        streamDialog.setContentView(R.layout.broadcast_dialog);

        Button btnJoinStream = streamDialog.findViewById(R.id.btnJoinStream);
        btnJoinStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StreamingActivity.class));
                streamDialog.dismiss();
                isJoinStream = true;
            }
        });

        TextView txtCancel = streamDialog.findViewById(R.id.txtCancel);
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamDialog.cancel();
                isJoinStream = false;
            }
        });

        streamDialog.show();
    }

    private void addControl() {
        viewPager = findViewById(R.id.viewpager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        splash_screen = findViewById(R.id.splash_screen);
        bottom_gradient_frame = findViewById(R.id.bottom_gradient_frame);
        miniPlayer_frame = findViewById(R.id.miniPlayer_frame);

        settingManager = SettingManager.getInstance(this);

        initialControllerCallback();
        initialConnectionCallback();
        initialMediaBrowser();

        addMiniPlayer();
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
                Log.d(LOG_TAG, "onConnected");

                // Get the token for the MediaSession
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
//                Log.d(LOG_TAG, "token: " + token.toString());

                // Create a MediaControllerCompat
                try {
                    mediaController =
                            new MediaControllerCompat(MainActivity.this, // Context
                                    token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

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
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
            }
        };
    }

    private void addMiniPlayer() {
        miniPlayer_frame.setVisibility(View.GONE);

        MiniPlayerFragment miniPlayerFragment = new MiniPlayerFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.miniPlayer_frame, miniPlayerFragment, MINI_PLAYER_TAG).commit();
    }

    private void addEvent() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.discover_item:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.search_item:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.library_item:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d("viewPager", "onPageScrolled: position: " + position + ", positionOffset: " + positionOffset + ", positionOffsetPixels: " + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(position).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                menuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("viewPager", "onPageScrollStateChanged: " + state);
            }
        });

        setupViewPager(viewPager);

        runSplashScreen();
    }

    private void runSplashScreen() {
//        Log.d("runSplashScreen", "openMainActivity");

        splash_screen.setVisibility(View.VISIBLE);
        bottomNavigationView.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                Log.d("runSplashScreen", "run");

                bottomNavigationView.setVisibility(View.VISIBLE);
                splash_screen.setVisibility(View.GONE);
            }
        }, SHOWING_INTERVAL);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        discoverFragment = new DiscoverFragment();
        searchFragment = new SearchFragment();
        libraryFragment = new LibraryFragment();

        adapter.addFragment(discoverFragment);
        adapter.addFragment(searchFragment);
        adapter.addFragment(libraryFragment);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(adapter.getCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
        EventBus.getDefault().post(new UpdateThemeEvent());
    }

    private void updateTheme() {
        Log.d("updateTheme", "updated");
        if (settingManager.isDarkTheme()) {
            bottomNavigationView.setItemBackgroundResource(R.color.colorDark2);
            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_dark);
        } else {
            bottomNavigationView.setItemBackgroundResource(R.color.colorLight2);
            bottom_gradient_frame.setBackgroundResource(R.drawable.lyric_hidden_bottom_gradient_light);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(ShowMiniplayerEvent showMiniplayerEvent) {
        miniPlayer_frame.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mediaBrowser.connect();

        if (nodePlayer == null) {
            initialNodePlayer();
        }
        nodePlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mediaBrowser.disconnect();

        nodePlayer.release();
        nodePlayer = null;
    }

    @Override
    public void onBackPressed() {
        if (Calendar.getInstance().getTimeInMillis() - lastBackPress <= BACK_PRESS_INTERVAL) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_LONG).show();
            lastBackPress = Calendar.getInstance().getTimeInMillis();
        }
    }
}