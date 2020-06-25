package com.albertkhang.tunedaily.utils;

import android.Manifest;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.networks.CheckFileSize;
import com.albertkhang.tunedaily.networks.DownloadTrack;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileWriter;

public class DownloadTrackManager {
    private static final String LOG_TAG = "DownloadTrackManager";
    private static final String FILENAME_EXTENSION = ".mp3";
    private static Context context;

    public DownloadTrackManager(Context context) {
        this.context = context;
    }

    public void downloadTrack(Track track) {
        if (isFileExists(track)) {
            //checkFileSize
            CheckFileSize checkFileSize = new CheckFileSize(context);
            checkFileSize.setOnPostExecuteListener(new CheckFileSize.OnPostExecuteListener() {
                @Override
                public void onPostExecuteListener(boolean isSameSize) {
                    if (isSameSize) {
                        Toast.makeText(context, "This song was downloaded.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "This song is downloading.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            checkFileSize.execute(track);
        } else {
            DownloadTrack downloadTrack = new DownloadTrack(context);
            downloadTrack.execute(track);
        }
    }

    public static File getFile(Track track) {
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

    public static boolean isFileExists(Track track) {
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
