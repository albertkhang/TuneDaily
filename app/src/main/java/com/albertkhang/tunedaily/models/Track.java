package com.albertkhang.tunedaily.models;

import java.io.Serializable;

public class Track implements Serializable {
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

    public Track(Track track) {
        this.id = track.getId();
        this.title = track.getTitle();
        this.album = track.getAlbum();
        this.artist = track.getArtist();
        this.genre = track.getGenre();
        this.duration = track.getDuration();
        this.track = track.getTrack();
        this.cover = track.getCover();
        this.type = track.getType();
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
