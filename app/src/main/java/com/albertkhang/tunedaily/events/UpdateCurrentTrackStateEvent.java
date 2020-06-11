package com.albertkhang.tunedaily.events;

import com.albertkhang.tunedaily.utils.Track;

public class UpdateCurrentTrackStateEvent {
    private Track track;

    public UpdateCurrentTrackStateEvent(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }
}
