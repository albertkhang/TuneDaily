package com.albertkhang.tunedaily.utils;

import android.util.Log;

import com.albertkhang.tunedaily.models.Playlist;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.models.UserSettings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;

public class PlaylistManager {
    private static final String PLAYLIST_REGEX = "[a-zA-Z0-9]([a-zA-Z0-9 ])*";
    private static PlaylistManager instance;
    private FirebaseAuth mAuth;
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface NAME {
        int EMPTY = 0;
        int NOT_VALID = 1;
        int VALID = 2;
        int EXIST = 3;
    }

    public interface PLAYLIST {
        String COVER = "cover";
        String TRACKS = "tracks";
        String PLAYLIST_NAMES = "playlist";
    }

    public PlaylistManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void deleteAllPlaylist() {
        List<String> arrayList = getAllPlaylist();
        Log.d("deleteAllPlaylist", "before: " + arrayList.toString());

        Paper.book().destroy();

        arrayList = getAllPlaylist();
        Log.d("deleteAllPlaylist", "after: " + arrayList.toString());

        Paper.book("liked_songs").destroy();
    }

    public void deletePlaylist(String name) {
        deletePlaylistRef(name);
        uploadRefToFirebase();
    }

    private void uploadRefToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            List<String> listName = PlaylistManager.getInstance().getAllPlaylist();

            Map<String, Playlist> data = new HashMap<>();
            for (int i = 0; i < listName.size(); i++) {
                String title = listName.get(i);
                String cover = PlaylistManager.getInstance().getPlaylistCover(listName.get(i));
                ArrayList<Integer> tracks = PlaylistManager.getInstance().getPlaylistTracks(listName.get(i));

                data.put(listName.get(i), new Playlist(-1, title, cover, tracks));
            }

            UserSettings userSettings = new UserSettings(true, true, new ArrayList<Integer>(), data);
            db.collection("users").document(user.getUid()).set(userSettings);
        }
    }

    private void deletePlaylistRef(String name) {
        Paper.book(name).destroy();

        List<String> arrayList = getAllPlaylist();
        arrayList.remove(name);
        Paper.book().write(PLAYLIST.PLAYLIST_NAMES, arrayList);
    }

    public static PlaylistManager getInstance() {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new PlaylistManager();
                }
            }
        }
        return instance;
    }

    public void addToLikedSongs(int id) {
        addToLikedSongsRef(id);
        addToLikedSongsFirebase();
    }

    private void addToLikedSongsFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference ref = db.collection("users").document(user.getUid());
            ref
                    .update("liked_songs", getLikedSongsIds())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("addToLikedSongsFirebase", "onSuccess");
                        }
                    });
        }
    }

    private void addToLikedSongsRef(int id) {
        List<Integer> tracks = getLikedSongsIds();

        List<Integer> temp = new ArrayList<>();
        temp.add(id);
        temp.addAll(tracks);

        Paper.book("liked_songs").write(PLAYLIST.TRACKS, temp);
    }

    public void removeFromLikedSongs(int id) {
        removeFromLikedSongsRef(id);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            removeFromLikedSongsFirebase(user);
        }
    }

    private void removeFromLikedSongsFirebase(FirebaseUser user) {
        DocumentReference ref = db.collection("users").document(user.getUid());
        ref
                .update("liked_songs", getLikedSongsIds())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("removeFromLikedSongs", "onSuccess");
                    }
                });
    }

    private void removeFromLikedSongsRef(int id) {
        List<Integer> tracks = getLikedSongsIds();
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i) == id) {
                tracks.remove(i);
                break;
            }
        }

        Paper.book("liked_songs").write(PLAYLIST.TRACKS, tracks);
    }

    public boolean isContainInLikedSongs(int id) {
        List<Integer> tracks = getLikedSongsIds();
        Log.d("Playlist", "tracks: " + tracks.toString());
        return tracks.contains(id);
    }

    public boolean isContainIPlaylist(int id, String playlistName) {
        List<Integer> tracks = getPlaylistTracks(playlistName);
//        Log.d(playlistName, "tracks: " + tracks.toString());
        return tracks.contains(id);
    }

    public List<Integer> getLikedSongsIds() {
        return Paper.book("liked_songs").read(PLAYLIST.TRACKS, new ArrayList<Integer>());
    }

    public List<Integer> getPlaylistIds(String title) {
        return Paper.book(title).read(PLAYLIST.TRACKS, new ArrayList<Integer>());
    }

    public int getLikedSongsSize() {
        return Paper.book("liked_songs").read(PLAYLIST.TRACKS, new ArrayList<Integer>()).size();
    }

    public int isPlaylistNameValidated(String playlistName) {
        if (!playlistName.isEmpty()) {
            if (playlistName.matches(PLAYLIST_REGEX)) {
                if (getAllPlaylist().size() == 0) {
                    return NAME.VALID;
                } else {
                    if (getAllPlaylist().contains(playlistName)) {
                        return NAME.EXIST;
                    } else {
                        return NAME.VALID;
                    }
                }
            } else {
                return NAME.NOT_VALID;
            }
        } else {
            return NAME.EMPTY;
        }
    }

    public List<String> getAllPlaylist() {
        return Paper.book().read(PLAYLIST.PLAYLIST_NAMES, new ArrayList<String>());
    }

    public void createNewPlaylist(String name) {
        createNewPlaylistRef(name);
        uploadRefToFirebase();
    }

    private void createNewPlaylistRef(String name) {
        Paper.book(name).write(PLAYLIST.COVER, "");
        Paper.book(name).write(PLAYLIST.TRACKS, new ArrayList<Integer>());

        List<String> arrayList = getAllPlaylist();
        arrayList.add(name);
        Paper.book().write(PLAYLIST.PLAYLIST_NAMES, arrayList);
    }

    public void addToFirstPlaylist(String name, int trackId) {
        addToFirstPlaylistRef(name, trackId);
        uploadRefToFirebase();
    }

    private void addToFirstPlaylistRef(String name, int trackId) {
        ArrayList ids = new ArrayList();
        ids.add(trackId);
        ids.addAll(getPlaylistTracks(name));
        Paper.book(name).write(PLAYLIST.TRACKS, ids);

        getTrack(name, trackId);
    }

    public interface OnCompletePlaylistCoverListener {
        void onCompletePlaylistCoverListener(String cover);
    }

    private OnCompletePlaylistCoverListener onCompletePlaylistCoverListener;

    public void setOnCompletePlaylistCoverListener(OnCompletePlaylistCoverListener onCompletePlaylistCoverListener) {
        this.onCompletePlaylistCoverListener = onCompletePlaylistCoverListener;
    }

    private void getTrack(final String name, int id) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(id);

        FirebaseManager.getInstance().setReadTrackFromIdsListener(new FirebaseManager.ReadTrackFromIdsListener() {
            @Override
            public void readTrackFromIdsListener(ArrayList<Track> tracks) {
                Log.d("PlaylistManager", tracks.get(0).toString());

                String cover = tracks.get(0).getCover();
                setPlaylistCover(name, cover);
                if (onCompletePlaylistCoverListener != null) {
                    onCompletePlaylistCoverListener.onCompletePlaylistCoverListener(cover);
                }
            }
        });

        FirebaseManager.getInstance().getTrackFromIds(ids);
    }

    public int getPlaylistTotal(String name) {
        return getPlaylistTracks(name).size();
    }

    public String getPlaylistCover(String name) {
        return Paper.book(name).read(PLAYLIST.COVER);
    }

    private void setPlaylistCover(String name, String url) {
        Paper.book(name).write(PLAYLIST.COVER, url);
    }

    public ArrayList<Integer> getPlaylistTracks(String name) {
        return Paper.book(name).read(PLAYLIST.TRACKS, new ArrayList<Integer>());
    }

    public void removeFromPlaylistTracks(String playlistName, int deletePosition) {
        ArrayList<Integer> integers = getPlaylistTracks(playlistName);
        integers.remove(deletePosition);

        Paper.book(playlistName).write(PLAYLIST.TRACKS, integers);
    }

    public void setPlaylistTracks(String playlistName, ArrayList<Integer> tracks) {
        Paper.book(playlistName).write(PLAYLIST.TRACKS, tracks);
    }
}
