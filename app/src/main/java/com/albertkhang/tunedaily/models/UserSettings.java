package com.albertkhang.tunedaily.models;

import java.util.List;
import java.util.Map;

public class UserSettings {
    private boolean isDark;
    private boolean isEnglish;
    private List<Integer> liked_songs;
    private Map<String, Playlist> playlists;

    public UserSettings(boolean isDark, boolean isEnglish, List<Integer> liked_songs, Map<String, Playlist> playlists) {
        this.isDark = isDark;
        this.isEnglish = isEnglish;
        this.liked_songs = liked_songs;
        this.playlists = playlists;
    }

    public boolean isDark() {
        return isDark;
    }

    public boolean isEnglish() {
        return isEnglish;
    }

    public List<Integer> getLiked_songs() {
        return liked_songs;
    }

    public Map<String, Playlist> getPlaylists() {
        return playlists;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "isDark=" + isDark +
                ", isEnglish=" + isEnglish +
                ", liked_songs=" + liked_songs +
                ", playlists=" + playlists +
                '}';
    }
}
