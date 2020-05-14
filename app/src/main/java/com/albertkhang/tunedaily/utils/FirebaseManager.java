package com.albertkhang.tunedaily.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirebaseManager {
    private static FirebaseManager instance;
    private static final int LIMIT_TOP_CHART = 5;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    public interface ReadTrackFromIds {
        void readTrackFromIds(ArrayList<Track> tracks);
    }

    private ReadTrackFromIds readTrackFromIds;

    public void setReadTrackFromIds(ReadTrackFromIds readTrackFromIds) {
        this.readTrackFromIds = readTrackFromIds;
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

    public ArrayList<Track> getTrackFromIds(List<Integer> ids) {
        final ArrayList<Track> topTracks = new ArrayList();

        db.collection("TuneDaily/tracks/track")
                .whereIn("id", ids)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("getTrackFromIds", "Failure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("getTrackFromIds", document.getId());
//                            Log.d("getTrackFromIds", "data: " + document.getData());

                            int id = (int) (long) document.get("id");
                            String title = (String) document.get("title");
                            String album = (String) document.get("album");
                            String artist = (String) document.get("artist");
                            String genre = (String) document.get("genre");
                            int duration = (int) (long) document.get("duration");
                            String track = (String) document.get("track");
                            String cover = (String) document.get("cover");
                            String type = (String) document.get("type");

                            duration /= 1000;

                            topTracks.add(new Track(
                                    id,
                                    title,
                                    album,
                                    artist,
                                    genre,
                                    duration,
                                    track,
                                    cover,
                                    type, null
                            ));
                        }

                        readTrackFromIds.readTrackFromIds(topTracks);

                        for (Track topTrack : topTracks) {
                            Log.d("getTrackFromIds", "data: " + topTrack.toString());
                        }
                    }
                });

        return topTracks;
    }
}