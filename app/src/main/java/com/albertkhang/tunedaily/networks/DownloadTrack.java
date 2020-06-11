package com.albertkhang.tunedaily.networks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.albertkhang.tunedaily.events.UpdateDownloadedTrack;
import com.albertkhang.tunedaily.utils.DownloadTrackManager;
import com.albertkhang.tunedaily.models.Track;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTrack extends AsyncTask<Track, Integer, Track> {
    private static final String LOG_TAG = "DownloadTrack";
    private Context context;
    private DownloadTrackManager downloadTrackManager;

    public DownloadTrack(Context context) {
        this.context = context;
        downloadTrackManager = new DownloadTrackManager(context);
    }

    @Override
    protected Track doInBackground(Track... tracks) {
        int count;
        try {
            URL url = new URL(tracks[0].getTrack());
            URLConnection connection = url.openConnection();
            connection.connect();

            DownloadTrackManager downloadTrackManager = new DownloadTrackManager(context);
            InputStream input = new BufferedInputStream(url.openStream(), 9999);
            OutputStream output = new FileOutputStream(downloadTrackManager.getFile(tracks[0]));
            byte data[] = new byte[1024];

            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tracks[0];
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPostExecute(Track track) {
        super.onPostExecute(track);
        Toast.makeText(context, "Downloaded \"" + track.getTitle() + "\"", Toast.LENGTH_SHORT).show();

        EventBus.getDefault().post(new UpdateDownloadedTrack());
        File file = downloadTrackManager.getFile(track);
        Log.d(LOG_TAG, "file size: " + file.length());
    }
}
