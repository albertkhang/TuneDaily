package com.albertkhang.tunedaily.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class PlaylistManager {
    private static final String PLAYLIST_REGEX = "[a-zA-Z0-9]([a-zA-Z0-9 ])*";
    private static PlaylistManager instance;
    private FirebaseAuth mAuth;

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

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            addToLikedSongsFirebase();
        }
    }

    private void addToLikedSongsFirebase() {

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
            removeFromLikedSongsFirebase(id);
        }
    }

    private void removeFromLikedSongsFirebase(int id) {

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

    public List<Integer> getLikedSongsIds() {
        return Paper.book("liked_songs").read(PLAYLIST.TRACKS, new ArrayList<Integer>());
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
        Paper.book(name).write(PLAYLIST.COVER, "");
        Paper.book(name).write(PLAYLIST.TRACKS, new ArrayList<Integer>());

        List<String> arrayList = getAllPlaylist();
        arrayList.add(name);
        Paper.book().write(PLAYLIST.PLAYLIST_NAMES, arrayList);
    }

    public void addToFirstPlaylist(final String name, int trackId) {
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
                onCompletePlaylistCoverListener.onCompletePlaylistCoverListener(cover);
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
}
