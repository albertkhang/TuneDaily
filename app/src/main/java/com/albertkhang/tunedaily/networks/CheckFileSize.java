package com.albertkhang.tunedaily.networks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.albertkhang.tunedaily.utils.DownloadTrackManager;
import com.albertkhang.tunedaily.models.Track;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class CheckFileSize extends AsyncTask<Track, Integer, Boolean> {
    private static final String LOG_TAG = "CheckFileSize";
    private Context context;
    private DownloadTrackManager downloadTrackManager;

    public CheckFileSize(Context context) {
        this.context = context;
        downloadTrackManager = new DownloadTrackManager(context);
    }

    @Override
    protected Boolean doInBackground(Track... tracks) {
        try {
            URL url = new URL(tracks[0].getTrack());
            URLConnection conection = url.openConnection();
            conection.connect();
            int lengthOfFile = conection.getContentLength();

            int fileSize = new DownloadTrackManager(context).getFileSize(tracks[0]);

            Log.d(LOG_TAG, "lengthOfFile: " + lengthOfFile + ", fileSize: " + fileSize);
            return lengthOfFile == fileSize;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public interface OnPostExecuteListener {
        void onPostExecuteListener(boolean isSameSize);
    }

    private OnPostExecuteListener onPostExecuteListener;

    public void setOnPostExecuteListener(OnPostExecuteListener onPostExecuteListener) {
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected void onPostExecute(Boolean isSameSize) {
        if (onPostExecuteListener != null) {
            onPostExecuteListener.onPostExecuteListener(isSameSize);
        }
    }
}
