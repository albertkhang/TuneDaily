package com.albertkhang.tunedaily.models;

import java.io.Serializable;
import java.util.List;

public class Playlist implements Serializable {
    private int id;
    private String title;
    private String cover;
    private List<Integer> tracks;

    public Playlist(int id, String title, String cover, List<Integer> tracks) {
        this.id = id;
        this.title = title;
        this.cover = cover;
        this.tracks = tracks;
    }

    public Playlist(Playlist playlist) {
        this.id = playlist.id;
        this.title = playlist.title;
        this.cover = playlist.cover;
        this.tracks = playlist.tracks;
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                ", tracks=" + tracks +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCover() {
        return cover;
    }

    public List<Integer> getTracks() {
        return tracks;
    }
}
