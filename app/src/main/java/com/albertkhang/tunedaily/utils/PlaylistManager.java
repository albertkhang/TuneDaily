package com.albertkhang.tunedaily.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class PlaylistManager {
    private static final String PLAYLIST_REGEX = "[a-zA-Z0-9]([a-zA-Z0-9 ])*";
    private static PlaylistManager instance;

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

    public static PlaylistManager getInstance(Context context) {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new PlaylistManager();
                }
            }
        }
        return instance;
    }

    private static final String LIKED_SONGS = "liked_songs";

    public int isPlaylistNameValidated(String playlistName) {
        if (!playlistName.isEmpty()) {
            if (playlistName.matches(PLAYLIST_REGEX)) {
                if (getAllPlaylistName().size() == 0) {
                    return NAME.VALID;
                } else {
                    if (getAllPlaylistName().contains(playlistName)) {
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

    public List<String> getAllPlaylistName() {
        return Paper.book().read(PLAYLIST.PLAYLIST_NAMES, new ArrayList<String>());
    }

    public void createNewPlaylist(String name) {
        Paper.book(name).write(PLAYLIST.COVER, "");
        Paper.book(name).write(PLAYLIST.TRACKS, new ArrayList<Integer>());

        List<String> arrayList = getAllPlaylistName();
        arrayList.add(name);
        Paper.book().write(PLAYLIST.PLAYLIST_NAMES, arrayList);
    }

    public int getPlaylistTotal(String name) {
        return getPlaylistTracks(name).size();
    }

    public String getPlaylistCover(String name) {
        return Paper.book(name).read(PLAYLIST.COVER);
    }

    public void setPlaylistCover(String name, String url) {
        Paper.book(name).write(PLAYLIST.COVER, url);
    }

    public ArrayList<Integer> getPlaylistTracks(String name) {
        return Paper.book(name).read(PLAYLIST.TRACKS);
    }
}
