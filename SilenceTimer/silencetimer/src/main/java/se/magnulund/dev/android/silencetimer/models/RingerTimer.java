package se.magnulund.dev.android.silencetimer.models;// Created by Gustav on 06/02/2014.

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import se.magnulund.dev.android.silencetimer.services.RingerTimerIntentService;
import se.magnulund.dev.android.silencetimer.utils.DateTimeUtil;

public class RingerTimer implements Parcelable {
    public static final String RINGER_TIMER = "ringer_timer";
    public static final Parcelable.Creator<RingerTimer> CREATOR = new Parcelable.Creator<RingerTimer>() {
        public RingerTimer createFromParcel(Parcel in) {
            return new RingerTimer(in);
        }

        public RingerTimer[] newArray(int size) {
            return new RingerTimer[size];
        }
    };
    private static final String TAG = "RingerTimer";
    private static final String TARGET_RINGER_MODE = "target_ringer_mode";
    private static final String CURRENT_RINGER_MODE = "current_ringer_mode";
    private static final String DURATION = "duration";
    private static final String START_TIMESTAMP = "start_timestamp";
    private static final String IS_RUNNING = "is_running";
    private int targetRingerMode;
    private int currentRingerMode;
    private long duration;
    private long startTimestamp;
    private long endTimestamp;
    private boolean isRunning;

    public RingerTimer(int targetRingerMode, int currentRingerMode, long duration, long startTimestamp) {
        this.targetRingerMode = targetRingerMode;
        this.currentRingerMode = currentRingerMode;
        this.duration = duration;
        this.startTimestamp = startTimestamp;
        this.isRunning = false;
    }

    private RingerTimer(Parcel in) {
        targetRingerMode = in.readInt();
        currentRingerMode = in.readInt();
        duration = in.readLong();
        startTimestamp = in.readLong();
        isRunning = in.readByte() != 0;
    }

    public static RingerTimer fromJSON(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        RingerTimer ringerTimer = new RingerTimer(
                jsonObject.getInt(TARGET_RINGER_MODE),
                jsonObject.getInt(CURRENT_RINGER_MODE),
                jsonObject.getLong(DURATION),
                jsonObject.getLong(START_TIMESTAMP)
        );
        ringerTimer.setRunning(jsonObject.getBoolean(IS_RUNNING));
        return ringerTimer;
    }

    public int getCurrentRingerMode() {
        return currentRingerMode;
    }

    public void setCurrentRingerMode(int currentRingerMode) {
        this.currentRingerMode = currentRingerMode;
    }

    public int getTargetRingerMode() {
        return targetRingerMode;
    }

    public void setTargetRingerMode(int ringerMode) {
        this.targetRingerMode = ringerMode;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getDurationHour() {
        return (int) Math.floor(duration / DateTimeUtil.MILLIS_PER_HOUR);
    }

    public int getDurationMinute() {
        return (int) Math.floor((duration - getDurationHour() * DateTimeUtil.MILLIS_PER_HOUR) / DateTimeUtil.MILLIS_PER_MINUTE);
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void setRunning(boolean running) {
        isRunning = running;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void start(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long startTime = SystemClock.elapsedRealtime() + duration;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            long startWindow = (DateTimeUtil.MILLIS_PER_MINUTE < duration / 20) ? DateTimeUtil.MILLIS_PER_MINUTE : duration / 20;
            alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, startWindow, getTimerIntent(context));
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, getTimerIntent(context));
        }

        endTimestamp = startTimestamp + duration;

        isRunning = true;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void stop(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getTimerIntent(context));
        isRunning = false;
    }

    private PendingIntent getTimerIntent(Context context) {
        Intent intent = RingerTimerIntentService.getRevertRingerModeIntent(context, targetRingerMode);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public boolean isExpired() {
        return startTimestamp + duration > Calendar.getInstance().getTimeInMillis();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TARGET_RINGER_MODE, targetRingerMode);
        jsonObject.put(CURRENT_RINGER_MODE, currentRingerMode);
        jsonObject.put(DURATION, duration);
        jsonObject.put(START_TIMESTAMP, startTimestamp);
        jsonObject.put(IS_RUNNING, isRunning);
        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(targetRingerMode);
        out.writeInt(currentRingerMode);
        out.writeLong(duration);
        out.writeLong(startTimestamp);
        out.writeLong((byte) (isRunning ? 1 : 0));
    }

    public void recalculateDuration() {
        long now = Calendar.getInstance().getTimeInMillis();
        duration = startTimestamp + duration - now;
        startTimestamp = now;
    }
}
