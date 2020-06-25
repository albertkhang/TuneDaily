package com.albertkhang.tunedaily.events;

import com.albertkhang.tunedaily.models.Track;

public class UpdateCurrentTrackEvent {
    private Track track;

    public UpdateCurrentTrackEvent(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }
}
