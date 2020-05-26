package com.albertkhang.tunedaily.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.util.Calendar;

public class BecomingNoisyReceiver extends BroadcastReceiver {
    public interface OnReceiveListener {
        void onReceiveListener();
    }

    private OnReceiveListener onReceiveListener;

    public void setOnReceiveListener(OnReceiveListener onReceiveListener) {
        this.onReceiveListener = onReceiveListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            // Pause the playback
            onReceiveListener.onReceiveListener();
        }
    }
}
