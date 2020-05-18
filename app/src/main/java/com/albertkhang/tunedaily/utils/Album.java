package com.albertkhang.tunedaily.utils;

import java.util.List;

public class Album {
    private int id;
    private String title;
    private String cover;
    private List<Integer> tracks;

    public Album(int id, String titile, String cover, List<Integer> tracks) {
        this.id = id;
        this.title = titile;
        this.cover = cover;
        this.tracks = tracks;
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
