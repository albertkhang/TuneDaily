package com.albertkhang.tunedaily.utils;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.albertkhang.tunedaily.activities.MainActivity;
import com.albertkhang.tunedaily.networks.CheckFileSize;
import com.albertkhang.tunedaily.networks.DownloadTrack;
import com.google.common.base.Charsets;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DownloadTrackManager {
    private static final String LOG_TAG = "DownloadTrackManager";
    private static final String FILENAME_EXTENSION = ".mp3";
    private Context context;

    public DownloadTrackManager(Context context) {
        this.context = context;
    }

    public String createNewFile(Track track) {
        File externalFile = context.getExternalFilesDir(null).getAbsoluteFile();

        String filedescription = track.getTitle();
        String filename = Hash.md5(track.getTrack());
        File file = new File(externalFile, filename.concat(FILENAME_EXTENSION));

        try {
            FileWriter writer = new FileWriter(file);
            writer.append(filedescription);
            writer.flush();
            writer.close();

            Log.d(LOG_TAG, "file: " + file.getAbsolutePath());
            return externalFile.getPath();
        } catch (Exception e) {
            Log.d(LOG_TAG, "e: " + e.toString());
        }

        Log.d(LOG_TAG, "file: " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public void downloadTrack(Track track) {
//        DownloadTrack downloadTrack = new DownloadTrack(context);
//        downloadTrack.execute(track);

        if (isFileExists(track)) {
            //checkFileSize
            CheckFileSize checkFileSize = new CheckFileSize(context);
            checkFileSize.execute(track);
        } else {
            DownloadTrack downloadTrack = new DownloadTrack(context);
            downloadTrack.execute(track);
        }
    }

    public File getFile(Track track) {
        File externalFile = context.getExternalFilesDir(null).getAbsoluteFile();
        String filename = Hash.md5(track.getTrack());
        return new File(externalFile, filename.concat(FILENAME_EXTENSION));
    }

    public int getFileSize(Track track) {
        File externalFile = context.getExternalFilesDir(null).getAbsoluteFile();
        String filename = Hash.md5(track.getTrack());
        File file = new File(externalFile, filename.concat(FILENAME_EXTENSION));

        return (int) file.length();
    }

    public boolean isFileExists(Track track) {
        return getFile(track).exists();
    }

    public void requestPermission(PermissionListener permissionlistener) {
        TedPermission.with(context)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not download songs.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }
}
