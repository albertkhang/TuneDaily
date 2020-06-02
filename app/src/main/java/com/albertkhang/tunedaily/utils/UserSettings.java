package com.albertkhang.tunedaily.utils;

import java.util.List;

public class UserSettings {
    private boolean isDark;
    private boolean isEnglish;
    private List<Integer> liked_songs;

    public UserSettings(boolean isDark, boolean isEnglish, List<Integer> liked_songs) {
        this.isDark = isDark;
        this.isEnglish = isEnglish;
        this.liked_songs = liked_songs;
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

    @Override
    public String toString() {
        return "UserSettings{" +
                "isDark=" + isDark +
                ", isEnglish=" + isEnglish +
                ", liked_songs=" + liked_songs +
                '}';
    }
}
