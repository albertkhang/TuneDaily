package com.albertkhang.tunedaily.utils;

public class PlaylistManager {
    private static final String PLAYLIST_REGEX = "[a-zA-Z0-9]([a-zA-Z0-9 ])*";

    public static final int NAME_EMPTY = 0;
    public static final int NAME_NOT_VALID = 1;
    public static final int NAME_VALID = 3;

    public static int isPlaylistNameValidated(String playlistName) {
        if (!playlistName.isEmpty()) {
            if (playlistName.matches(PLAYLIST_REGEX)) {
                return NAME_VALID;
            } else {
                return NAME_NOT_VALID;
            }
        } else {
            return NAME_EMPTY;
        }
    }
}
