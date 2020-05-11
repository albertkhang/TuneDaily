package com.albertkhang.tunedaily.fragments;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.albertkhang.tunedaily.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class FragmentFullPlayer extends Fragment {
    private static final String PATH = "https://firebasestorage.googleapis.com/v0/b/dynamic-density-271310.appspot.com/o/songs%2FThuanTheoYTroi-BuiAnhTuan-6150266.mp3?alt=media&token=1a3d42f0-27b0-449c-84a4-89475fccf2e6";
    private static final String prefix = "https://firebasestorage.googleapis.com/v0/b/dynamic-density-271310.appspot.com/o/songs%2F";
    private static final String postfix = "?alt=media&token=1a3d42f0-27b0-449c-84a4-89475fccf2e6";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_full_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl();
        addEvent();
    }

    private void addControl() {
        testMetaDataFireBase();
    }

    private void addEvent() {

    }

    private void testMetaDataFireBase() {
        Log.d("MetaData", "testMetaDataFireBase");

//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference();
//
//        String fileName = storageRef.child("songs/ThuanTheoYTroi-BuiAnhTuan-6150266.mp3").getName();
//
//        String url = prefix + fileName + postfix;
//        Log.d("MetaData", "getName " + prefix + fileName + postfix);

//        storageRef.child("songs/ThuanTheoYTroi-BuiAnhTuan-6150266.mp3").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Log.d("MetaData", "onSuccess");
//                Log.d("MetaData", "getPath " + uri.getPath());
//
//                MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), uri);
//                Log.d("MetaData", "getDuration " + mediaPlayer.getDuration());
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Log.d("MetaData", "onFailure: " + exception);
//            }
//        });

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(PATH);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }
}

