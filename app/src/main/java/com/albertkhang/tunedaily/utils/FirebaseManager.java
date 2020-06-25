package com.albertkhang.tunedaily.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.albertkhang.tunedaily.models.Playlist;
import com.albertkhang.tunedaily.models.TopTrack;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.models.UserSettings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FirebaseManager {
    private static FirebaseManager instance;
    private static final int LIMIT_TOP_CHART = 5;
    private static final int LIMIT_RANDOM_ALBUM = 5;
    private static final int LIMIT_RANDOM_SONGS = 5;
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    public static void createDefaultUserSetting(String uid) {
        if (!uid.equals("")) {
            List<String> listName = PlaylistManager.getInstance().getAllPlaylist();

            Map<String, Playlist> data = new HashMap<>();
            for (int i = 0; i < listName.size(); i++) {
                String title = listName.get(i);
                String cover = PlaylistManager.getInstance().getPlaylistCover(listName.get(i));
                ArrayList<Integer> tracks = PlaylistManager.getInstance().getPlaylistTracks(listName.get(i));

                data.put(listName.get(i), new Playlist(-1, title, cover, tracks));
            }

            UserSettings userSettings = new UserSettings(true, true, new ArrayList<Integer>(), data);
            db.collection("users").document(uid).set(userSettings);
        }
    }

    public interface ReadTopTrackIdsListener {
        void updateTopTrackListener(ArrayList<TopTrack> topChartIdsOrdered);
    }

    private ReadTopTrackIdsListener readTopTrackIdsListener;

    public void setReadTopTrackIdsListener(ReadTopTrackIdsListener readTopTrackIdsListener) {
        this.readTopTrackIdsListener = readTopTrackIdsListener;
    }

    public interface ReadTrackFromIdsListener {
        void readTrackFromIdsListener(ArrayList<Track> tracks);
    }

    private ReadTrackFromIdsListener readTrackFromIdsListener;

    public void setReadTrackFromIdsListener(ReadTrackFromIdsListener readTrackFromIdsListener) {
        this.readTrackFromIdsListener = readTrackFromIdsListener;
    }

    public interface ReadRandomAlbumListener {
        void readRandomAlbumListener(ArrayList<Playlist> playlists);
    }

    private ReadRandomAlbumListener readRandomAlbumListener;

    public void setReadRandomAlbumListener(ReadRandomAlbumListener readRandomAlbumListener) {
        this.readRandomAlbumListener = readRandomAlbumListener;
    }

    public interface ReadPopularAlbumsListener {
        void readPopularAlbumsListener(ArrayList<Playlist> playlists);
    }

    private ReadPopularAlbumsListener readPopularAlbumsListener;

    public void setReadPopularAlbumsListener(ReadPopularAlbumsListener readPopularAlbumsListener) {
        this.readPopularAlbumsListener = readPopularAlbumsListener;
    }

    public interface ReadBestOfArtistListener {
        void readBestOfArtistListener(ArrayList<Playlist> playlists);
    }

    private ReadBestOfArtistListener readBestOfArtistListener;

    public void setReadBestOfArtistListener(ReadBestOfArtistListener readBestOfArtistListener) {
        this.readBestOfArtistListener = readBestOfArtistListener;
    }

    public interface ReadRandomArtistsListener {
        void readRandomArtistsListener(ArrayList<Playlist> playlists);
    }

    private ReadRandomArtistsListener readRandomArtistsListener;

    public void setReadRandomArtistsListener(ReadRandomArtistsListener readRandomArtistsListener) {
        this.readRandomArtistsListener = readRandomArtistsListener;
    }

    public interface ReadRandomSongsListener {
        void readRandomSongsListener(ArrayList<Track> tracks);
    }

    private ReadRandomSongsListener readRandomSongsListener;

    public void setReadRandomSongsListener(ReadRandomSongsListener readRandomSongsListener) {
        this.readRandomSongsListener = readRandomSongsListener;
    }

    public interface ReadByTitleListener {
        void readTrackByTitleListener(ArrayList<Track> tracks);

        void readAlbumByTitleListener(ArrayList<Playlist> playlists);

        void handleSearchIsEmptyListener();
    }

    private ReadByTitleListener readByTitleListener;

    public void setReadByTitleListener(ReadByTitleListener readByTitleListener) {
        this.readByTitleListener = readByTitleListener;
    }

    public void searchTrackByTitle(String s) {
//        Log.d("searchTrackByTitle", "s: " + s);

        if (!s.equals("")) {
            isSearchTracksEmpty = false;

            db.collection("TuneDaily/tracks/track")
                    .whereArrayContains("search_key", s)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot documents, @Nullable FirebaseFirestoreException e) {
                            Log.d("searchTrackByTitle", "size: " + documents.size());
                            ArrayList<Track> tracks = new ArrayList<>();

                            for (QueryDocumentSnapshot document : documents) {
                                Log.d("searchTrackByTitle", document.getId() + "=> " + document.get("title"));

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

                                tracks.add(new Track(
                                        id,
                                        title,
                                        album,
                                        artist,
                                        genre,
                                        duration,
                                        track,
                                        cover,
                                        type
                                ));
                            }

                            Log.d("searchTrackByTitle", tracks.toString());

                            if (tracks.size() == 0) {
                                isSearchTracksEmpty = true;

                                if (isSearchAlbumsEmpty) {
                                    readByTitleListener.handleSearchIsEmptyListener();
                                }
                            }

                            readByTitleListener.readTrackByTitleListener(tracks);
                        }
                    });
        }
    }

    private volatile boolean isSearchTracksEmpty = false;
    private volatile boolean isSearchAlbumsEmpty = false;

    public void searchAlbumByTitle(String s) {
//        Log.d("searchAlbumByTitle", "s: " + s);

        if (!s.equals("")) {
            isSearchAlbumsEmpty = false;

            db.collection("TuneDaily/albums/album")
                    .whereArrayContains("search_key", s)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot documents, @Nullable FirebaseFirestoreException e) {
                            Log.d("searchAlbumByTitle", "size: " + documents.size());
                            ArrayList<Playlist> playlists = new ArrayList<>();

                            for (QueryDocumentSnapshot document : documents) {
                                Log.d("searchAlbumByTitle", document.getId() + "=> " + document.get("title"));

                                int id = (int) (long) document.get("id");
                                String title = (String) document.get("title");
                                List<Integer> tracks = (List<Integer>) document.get("tracks");
                                String cover = (String) document.get("cover");

                                playlists.add(new Playlist(id, title, cover, tracks));
                            }

                            if (playlists.size() == 0) {
                                isSearchAlbumsEmpty = true;

                                if (isSearchTracksEmpty) {
                                    readByTitleListener.handleSearchIsEmptyListener();
                                }
                            }

                            readByTitleListener.readAlbumByTitleListener(playlists);
                        }
                    });
        }
    }

    public void getTopTrack() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("top_chart");
        ref.keepSynced(true);
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
                        readTopTrackIdsListener.updateTopTrackListener(topChartsOrdered);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("getTopTrackIds", "onCancelled: " + databaseError);
                    }
                });
    }

    public void getTrackFromIds(List<Integer> ids) {
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
                                    type
                            ));
                        }

                        readTrackFromIdsListener.readTrackFromIdsListener(topTracks);

                        for (Track topTrack : topTracks) {
                            Log.d("getTrackFromIds", "data: " + topTrack.toString());
                        }
                    }
                });

    }

    public void getPopularAlbums() {
        final List<Integer> ids = new ArrayList<>();
        final ArrayList<Playlist> results = new ArrayList<>();

        for (int i = 0; i < LIMIT_RANDOM_ALBUM; i++) {
            Random rn = new Random();
            ids.add(rn.nextInt(30 + 1));
        }

        Log.d("getPopularAlbums", "ids: " + ids);

        db.collection("TuneDaily/albums/album")
                .whereIn("id", ids)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("getPopularAlbums", "Failure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("getRandomAlbum", document.getId());

//                            Log.d("getRandomAlbum", "id: " + document.get("id"));
//                            Log.d("getRandomAlbum", "title: " + document.get("title"));
//                            Log.d("getRandomAlbum", "tracks: " + document.get("tracks"));
//                            Log.d("getRandomAlbum", "cover: " + document.get("cover"));

                            int id = (int) (long) document.get("id");
                            String title = (String) document.get("title");
                            List<Integer> tracks = (List<Integer>) document.get("tracks");
                            String cover = (String) document.get("cover");

                            results.add(new Playlist(id, title, cover, tracks));
                        }

                        readPopularAlbumsListener.readPopularAlbumsListener(results);
                        Log.d("getPopularAlbums", results.toString());
                    }
                });
    }

    public void getBestOfArtist() {
        final List<Integer> ids = new ArrayList<>();
        final ArrayList<Playlist> results = new ArrayList<>();

        for (int i = 0; i < LIMIT_RANDOM_ALBUM; i++) {
            Random rn = new Random();
            ids.add(rn.nextInt(30 + 1));
        }

        Log.d("getBestOfArtist", "ids: " + ids);

        db.collection("TuneDaily/albums/album")
                .whereIn("id", ids)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("getBestOfArtist", "Failure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("getRandomAlbum", document.getId());

                            int id = (int) (long) document.get("id");
                            String title = (String) document.get("title");
                            List<Integer> tracks = (List<Integer>) document.get("tracks");
                            String cover = (String) document.get("cover");

                            results.add(new Playlist(id, title, cover, tracks));
                        }

                        readBestOfArtistListener.readBestOfArtistListener(results);
                        Log.d("getBestOfArtist", results.toString());
                    }
                });
    }

    public void getRandomArtist() {
        final List<Integer> ids = new ArrayList<>();
        final ArrayList<Playlist> results = new ArrayList<>();

        for (int i = 0; i < LIMIT_RANDOM_ALBUM; i++) {
            Random rn = new Random();
            ids.add(rn.nextInt(30 + 1));
        }

        Log.d("getRandomArtist", "ids: " + ids);

        db.collection("TuneDaily/albums/album")
                .whereIn("id", ids)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("getRandomArtist", "Failure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("getRandomArtist", document.getId());

//                            Log.d("getRandomArtist", "id: " + document.get("id"));
//                            Log.d("getRandomArtist", "title: " + document.get("title"));
//                            Log.d("getRandomArtist", "tracks: " + document.get("tracks"));
//                            Log.d("getRandomArtist", "cover: " + document.get("cover"));

                            int id = (int) (long) document.get("id");
                            String title = (String) document.get("title");
                            List<Integer> tracks = (List<Integer>) document.get("tracks");
                            String cover = (String) document.get("cover");

                            results.add(new Playlist(id, title, cover, tracks));
                        }

                        readRandomArtistsListener.readRandomArtistsListener(results);
                        Log.d("getRandomArtist", results.toString());
                    }
                });
    }

    public void getRandomSongs() {
        final List<Integer> ids = new ArrayList<>();
        final ArrayList<Track> results = new ArrayList<>();

        for (int i = 0; i < LIMIT_RANDOM_SONGS; i++) {
            Random rn = new Random();
            ids.add(rn.nextInt(100 + 1));
        }

        Log.d("getRandomSongs", "ids: " + ids);

        db.collection("TuneDaily/tracks/track")
                .whereIn("id", ids)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("getRandomSongs", "Failure: ", e);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("getRandomSongs", document.getId());
//                            Log.d("getRandomSongs", "data: " + document.getData());

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

                            results.add(new Track(
                                    id,
                                    title,
                                    album,
                                    artist,
                                    genre,
                                    duration,
                                    track,
                                    cover,
                                    type
                            ));
                        }

                        readRandomSongsListener.readRandomSongsListener(results);
                    }
                });
    }
}