package com.albertkhang.tunedaily.utils;

public class TimeConverter {
    private static TimeConverter instance;

    public static TimeConverter getInstance() {
        if (instance == null) {
            synchronized (TimeConverter.class) {
                if (instance == null) {
                    instance = new TimeConverter();
                }
            }
        }
        return instance;
    }

    public String getTimestamp(int duration) {
        int min = duration / 60;
        int sec = duration % 60;

        String sMin = "";
        String sSec = "";

        if (min < 10) {
            sMin = "0" + min;
        } else {
            sMin = String.valueOf(min);
        }

        if (sec < 10) {
            sSec = "0" + sec;
        } else {
            sSec = String.valueOf(sec);
        }

        return sMin + ":" + sSec;
    }
}
