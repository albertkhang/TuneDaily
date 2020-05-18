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

    public Track(int id, String title, String album, String artist, String genre, int duration, String track, String cover, String type) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.track = track;
        this.cover = cover;
        this.type = type;
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

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", duration=" + duration +
                ", track='" + track + '\'' +
                ", cover='" + cover + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
