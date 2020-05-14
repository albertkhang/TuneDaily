package com.albertkhang.tunedaily.utils;

import androidx.annotation.NonNull;

import java.util.List;

public class Track {
    private int id;
    private String title;
    private String album;
    private String artist;
    private String genre;
    private int duration;
    private String track;
    private String cover;
    private String type;
    private List<String> search_key;

    public Track(int id, String title, String album, String artist, String genre, int duration, String track, String cover, String type, List<String> search_key) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.track = track;
        this.cover = cover;
        this.type = type;
        this.search_key = search_key;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public int getDuration() {
        return duration;
    }

    public String getTrack() {
        return track;
    }

    public String getCover() {
        return cover;
    }

    public String getType() {
        return type;
    }

    public List<String> getSearch_key() {
        return search_key;
    }

    @NonNull
    @Override
    public String toString() {
        return "Track["
                + id + ", "
                + title + ", "
                + album + ", "
                + artist + ", "
                + genre + ", "
                + duration + ", "
                + track + ", "
                + cover + ", "
                + type + ", "
                + search_key + "]";
    }
}
