package com.albertkhang.tunedaily.utils;

public class Playlist {
    private String name;
    private int total;
    private String cover;

    public Playlist(String name, int total, String cover) {
        this.name = name;
        this.total = total;
        this.cover = cover;
    }

    public Playlist(Playlist playlist) {
        this.name = playlist.name;
        this.total = playlist.total;
        this.cover = playlist.cover;
    }

    public String getName() {
        return name;
    }

    public int getTotal() {
        return total;
    }

    public String getCover() {
        return cover;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "name='" + name + '\'' +
                ", total=" + total +
                ", cover='" + cover + '\'' +
                '}';
    }
}
