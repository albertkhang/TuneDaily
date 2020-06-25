package com.albertkhang.tunedaily.events;

public class UpdateTitleArtistEvent {
    private String title;
    private String artist;

    public UpdateTitleArtistEvent(String title, String artist) {
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
