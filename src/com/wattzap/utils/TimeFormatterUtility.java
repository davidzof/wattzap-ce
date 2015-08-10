package com.wattzap.utils;

public final class TimeFormatterUtility {

    public static String timeToString(long time) {
        StringBuilder sb = new StringBuilder(8);
        long hours = time / 3600000;
        long minutes = (time - hours * 3600000) / 60000;
        long seconds = (time - hours * 3600000 - minutes * 60000) / 1000;

        if (hours < 10)
            sb.append('0');
        sb.append(hours);
        sb.append(':');

        if (minutes < 10)
            sb.append('0');
        sb.append(minutes);
        sb.append(':');

        if (seconds < 10)
            sb.append('0');
        sb.append(seconds);

        String formattedTime = sb.toString();
        return formattedTime;
    }
}