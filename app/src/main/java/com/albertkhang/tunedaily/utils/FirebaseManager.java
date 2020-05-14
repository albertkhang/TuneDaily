package com.albertkhang.tunedaily.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class FirebaseManager {
    private static FirebaseManager instance;
    private static final int LIMIT_TOP_CHART = 5;

    public static FirebaseManager getInstance() {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new FirebaseManager();
                }
            }
        }
        return instance;
    }

    public interface UpdateTopTrackIds {
        void updateTopTrack(ArrayList<TopTrack> topChartIdsOrdered);
    }

    private UpdateTopTrackIds updateTopTrackIds;

    public void setUpdateTopTrackIds(UpdateTopTrackIds updateTopTrackIds) {
        this.updateTopTrackIds = updateTopTrackIds;
    }

    public void getTopTrack() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("top_chart");
        Log.d("getTopTrackIds", ref.toString());

        ref.orderByChild("value").limitToLast(LIMIT_TOP_CHART)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("getTopTrackIds", "onDataChange");
                        Log.d("getTopTrackIds", "dataSnapshot.value: " + dataSnapshot.getValue());
                        Log.d("getTopTrackIds", "getChildrenCount: " + dataSnapshot.getChildrenCount());

                        ArrayList<TopTrack> topChartsOrdered = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            TopTrack track = snapshot.getValue(TopTrack.class);

                            if (track != null) {
                                topChartsOrdered.add(track);
                                Log.d("getTopTrackIds", "getId: " + track.getId());
                                Log.d("getTopTrackIds", "getValue: " + track.getValue());
                            }
                        }

                        Collections.sort(topChartsOrdered);
                        updateTopTrackIds.updateTopTrack(topChartsOrdered);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("getTopTrackIds", "onCancelled: " + databaseError);
                    }
                });
    }
}