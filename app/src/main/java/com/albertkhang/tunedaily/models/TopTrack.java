package com.albertkhang.tunedaily.models;

public class TopTrack implements Comparable<TopTrack> {
    private int id;
    private int value;

    public TopTrack() {
    }

    public TopTrack(int id, int value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TopTrack{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }

    @Override
    public int compareTo(TopTrack topTrack) {
        return topTrack.value - this.value;
    }
}
