package com.albertkhang.tunedaily.events;

import com.albertkhang.tunedaily.utils.Track;

public class UpdateCurrentTrack {
    private Track track;

    public UpdateCurrentTrack(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }
}
