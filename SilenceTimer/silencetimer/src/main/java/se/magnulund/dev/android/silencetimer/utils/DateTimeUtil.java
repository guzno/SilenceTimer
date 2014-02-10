package se.magnulund.dev.android.silencetimer.utils;// Created by Gustav on 04/02/2014.

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

import se.magnulund.dev.android.silencetimer.models.RingerTimer;

public class DateTimeUtil {

    private static final String TAG = "DateTimeUtil";

    public static final long NANOS_PER_MILLI = 1000000;
    public static final long NANOS_PER_SECOND = 1000 * NANOS_PER_MILLI;
    public static final long NANOS_PER_MINUTE = 60 * NANOS_PER_SECOND;

    public static final int MILLIS_PER_SECOND = 1000;
    public static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final int MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final int MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    public static final String DATE_MONTHNAME_DAY = "MMM d";
    public static final String TIME_HOUR_MINUTE = "HH:mm";
    public static final String TIME_HUR_MINUTE_AMPM = "hh:mm a";

    public static final String TIME_HOUR_MINUTE_SECONDS = "kk:mm:ss";

    public static String getExpirryTimeString(Context context, RingerTimer ringerTimer) {
        SimpleDateFormat simpleDateFormat;
        if (DateFormat.is24HourFormat(context)){
            simpleDateFormat = new SimpleDateFormat(TIME_HOUR_MINUTE);
        } else {
            simpleDateFormat = new SimpleDateFormat(TIME_HUR_MINUTE_AMPM);
        }
        Date date = new Date(ringerTimer.getEndTimestamp());
        return simpleDateFormat.format(date);
    }

    public static String getDurationString(long duration) {

        int days = (int)  Math.floor(duration / MILLIS_PER_DAY);

        duration = duration - days * MILLIS_PER_DAY;

        String durationString = (days > 0) ? Integer.valueOf(days) + " d" : "";

        int hours = (int) Math.floor(duration / MILLIS_PER_HOUR);

        duration = duration - hours * MILLIS_PER_HOUR;

        durationString += (hours > 0) ? ((durationString.length()>0)? " " : "") + hours + " h" : "";

        int minutes = (int)  Math.floor(duration / MILLIS_PER_MINUTE);

        durationString += (minutes > 0) ? ((durationString.length()>0)? " " : "") + minutes + " min" : "";

        return durationString;
    }
}