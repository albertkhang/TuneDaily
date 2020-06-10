package com.albertkhang.tunedaily.events;

public class UpdateFavouriteTrack {
    private int id;

    public UpdateFavouriteTrack(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
