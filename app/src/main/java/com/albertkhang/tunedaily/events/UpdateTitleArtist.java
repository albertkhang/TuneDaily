package com.albertkhang.tunedaily.events;

public class UpdateTitleArtist {
    private String title;
    private String artist;

    public UpdateTitleArtist(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
