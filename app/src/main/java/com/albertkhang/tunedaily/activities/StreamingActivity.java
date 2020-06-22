package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.TimeConverter;

import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

public class StreamingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "StreamingActivity";

    private NodePlayerView npvPlayerView;
    private NodePlayer nodePlayer;
    private static final String STREAMING_URL = "rtmp://45.76.150.28/live/android";

    private ConstraintLayout video_controller_frame;
    private ImageView imgPlayPause;
    private ImageView imgVolume;
    private SeekBar seekbar;
    private TextView txtVolumePercent;
    private ImageView imgFullscreen;
    private ImageView imgBack;

    private static boolean isEnableVolume = true;
    private boolean isFullscreen = false;

    private Handler handler;
    private Runnable controllerRunnable;
    private final long CONTROLLER_INTERVAL = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);

        addControl();
        addEvent();
    }

    private void addControl() {
        npvPlayerView = findViewById(R.id.npvPlayerView);
        npvPlayerView.setRenderType(NodePlayerView.RenderType.SURFACEVIEW);
        npvPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        video_controller_frame = findViewById(R.id.video_controller_frame);
        imgPlayPause = findViewById(R.id.imgPlayPause);
        imgVolume = findViewById(R.id.imgVolume);
        seekbar = findViewById(R.id.seekbar);
        seekbar = findViewById(R.id.seekbar);
        seekbar.setMax(100);
        seekbar.setProgress(80);
        txtVolumePercent = findViewById(R.id.txtVolumePercent);
        imgFullscreen = findViewById(R.id.imgFullscreen);
        imgBack = findViewById(R.id.imgBack);

        handler = new Handler();
        initialControllerRunnable();
        controllerRunnable.run();
    }

    private void initialControllerRunnable() {
        controllerRunnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        video_controller_frame.setVisibility(View.INVISIBLE);
                    }
                }, CONTROLLER_INTERVAL);
            }
        };
    }

    private void initialNodePlayer() {
        nodePlayer = new NodePlayer(this);
        nodePlayer.setInputUrl(STREAMING_URL);
        nodePlayer.setPlayerView(npvPlayerView);
        nodePlayer.setAudioEnable(true);
        nodePlayer.setVideoEnable(true);
        nodePlayer.setVolume(0.8f);
        nodePlayer.setNodePlayerDelegate(new NodePlayerDelegate() {
            @Override
            public void onEventCallback(NodePlayer player, int event, String msg) {
                Log.d(LOG_TAG, "event: " + event + ", msg: " + msg);

                switch (event) {
                    case 1001://NetConnection.Connect.Success
                        break;

                    case 1003://NetConnection.Connect.Reconnection
                        finish();
                        break;
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvent() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imgFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFullscreen) {
                    imgFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
                    isFullscreen = true;
                } else {
                    isFullscreen = false;
                    imgFullscreen.setImageResource(R.drawable.ic_fullscreen);
                }
            }
        });

        imgVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnableVolume) {
                    isEnableVolume = false;
                    imgVolume.setImageResource(R.drawable.ic_volume_off);
                    txtVolumePercent.setText("0%");
                } else {
                    isEnableVolume = true;
                    imgVolume.setImageResource(R.drawable.ic_volume_up);
                    txtVolumePercent.setText(seekbar.getProgress() + "%");
                }

                nodePlayer.setAudioEnable(isEnableVolume);
            }
        });

        imgVolume.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
//                        Log.d(LOG_TAG, "ACTION_UP");
                        controllerRunnable.run();
                        break;

                    case MotionEvent.ACTION_DOWN:
//                        Log.d(LOG_TAG, "ACTION_DOWN");
                        handler.removeCallbacksAndMessages(null);
                        break;
                }
                return false;
            }
        });

        imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nodePlayer.isPlaying()) {
                    nodePlayer.pause();
                    imgPlayPause.setImageResource(R.drawable.ic_play);
                } else {
//                    Log.d(LOG_TAG, "duration: " + nodePlayer.get());
                    nodePlayer.start();
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        imgPlayPause.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
//                        Log.d(LOG_TAG, "ACTION_UP");
                        controllerRunnable.run();
                        break;

                    case MotionEvent.ACTION_DOWN:
//                        Log.d(LOG_TAG, "ACTION_DOWN");
                        handler.removeCallbacksAndMessages(null);
                        break;
                }
                return false;
            }
        });

        npvPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video_controller_frame.getVisibility() == View.INVISIBLE) {
                    video_controller_frame.setVisibility(View.VISIBLE);
                    controllerRunnable.run();
                }
            }
        });

        video_controller_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                handler.removeCallbacks(controllerRunnable);
            }
        });

        video_controller_frame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
//                        Log.d(LOG_TAG, "ACTION_UP");
                        controllerRunnable.run();
                        break;

                    case MotionEvent.ACTION_DOWN:
//                        Log.d(LOG_TAG, "ACTION_DOWN");
                        handler.removeCallbacksAndMessages(null);
                        break;
                }
                return false;
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    txtVolumePercent.setText(progress + "%");

                    if (progress == 0) {
                        imgVolume.setImageResource(R.drawable.ic_volume_off);
                    } else {
                        imgVolume.setImageResource(R.drawable.ic_volume_up);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                nodePlayer.setVolume(seekBar.getProgress() / 100f);
                controllerRunnable.run();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        nodePlayer.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (nodePlayer == null) {
            initialNodePlayer();
        }
        nodePlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        nodePlayer.release();
        nodePlayer = null;
    }
}