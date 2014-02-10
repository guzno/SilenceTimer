package se.magnulund.dev.android.silencetimer.preferences;// Created by Gustav on 06/02/2014.

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;

import java.util.Calendar;

import se.magnulund.dev.android.silencetimer.models.RingerTimer;
import se.magnulund.dev.android.silencetimer.utils.DateTimeUtil;

public class Prefs {
    public static final String KEY_PREF_ENABLED = "pref_key_enabled";
    public static final String KEY_PREF_AUTO_TIMER_ENABLED = "pref_key_auto_timer_enabled";
    public static final String KEY_PREF_DEFAULT_TIMER = "pref_key_default_timer";
    public static final String KEY_PREF_ALWAYS_REVERT_TO_NORMAL = "pref_key_always_revert_to_normal";
    public static final String KEY_DEFAULT_TIMER = "key_default_timer";
    private static final String TAG = "Prefs";
    private static final String KEY_PREF_INITIALIZED = "pref_key_initialized";
    private static final String KEY_VERSION = "pref_version";
    private static final String KEY_PREVIOUS_RINGER_MODE = "key_previous_ringer_mode";
    private static final String KEY_CURRENT_RINGER_MODE = "key_current_ringer_mode";
    private static final String KEY_RINGER_TIMER_ACTIVE = "key_ringer_timer_active";
    private static final String KEY_RINGER_TIMER = "key_ringer_timer";
    private static final String KEY_RINGER_CHANGED = "key_ringer_changed";

    private final SharedPreferences prefs;

    private Prefs(Context context) {
        PackageManager packageManager = context.getPackageManager();
        assert packageManager != null;
        int versionCode;
        try {
            versionCode = packageManager.getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionCode = 1;
        }
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean initializedNow = intializePrefsIfNeeded(context, versionCode);
        if (!initializedNow) {
            boolean updateComplete = updatePrefsIfNeeded(context, versionCode);
            if (!updateComplete) {
                reInitializePrefs(context, versionCode);
            }
        }
    }

    public static Prefs get(Context context) {
        return new Prefs(context);
    }

    public boolean alwaysRevertToNormal() {
        return prefs.getBoolean(KEY_PREF_ALWAYS_REVERT_TO_NORMAL, false);
    }

    public boolean isAppEnabled() {
        return prefs.getBoolean(KEY_PREF_ENABLED, false);
    }

    public boolean isAutoModeEnabled() {
        return prefs.getBoolean(KEY_PREF_AUTO_TIMER_ENABLED, false);
    }

    public RingerTimer getDefaultTimer(int targetMode, int currentMode) {
        long duration = prefs.getLong(KEY_DEFAULT_TIMER, 60) * DateTimeUtil.MILLIS_PER_MINUTE;
        return new RingerTimer(targetMode, currentMode, duration, Calendar.getInstance().getTimeInMillis());
    }

    public int getPreviousRingerMode() {
        return prefs.getInt(KEY_PREVIOUS_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL);
    }

    public void setPreviousRingerMode(int ringerMode) {
        prefs.edit().putInt(KEY_PREVIOUS_RINGER_MODE, ringerMode).commit();
    }

    public void updateRingerModes(int newRingerMode) {

        final int oldRingerMode = getCurrentRingerMode();

        setPreviousRingerMode(oldRingerMode);
        setCurrentRingerMode(newRingerMode);
    }

    public RingerTimer getRingerTimer() {
        String json = prefs.getString(KEY_RINGER_TIMER, null);
        if (json != null) {
            try {
                return RingerTimer.fromJSON(json);
            } catch (JSONException e) {
                Log.d(TAG, "Invalid json string: " + json);
                return null;
            }
        } else {
            return null;
        }
    }

    public void setRingerTimer(RingerTimer ringerTimer) {
        try {
            prefs.edit().putString(KEY_RINGER_TIMER, ringerTimer.toJSON().toString()).commit();
            setRingerTimerActive(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearRingerTimer() {
        setRingerTimerActive(false);
        prefs.edit().putString(KEY_RINGER_TIMER, null).commit();
    }

    public int getCurrentRingerMode() {
        return prefs.getInt(KEY_CURRENT_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL);
    }

    public void setCurrentRingerMode(int ringerMode) {
        prefs.edit().putInt(KEY_CURRENT_RINGER_MODE, ringerMode).commit();
    }

    public boolean isRingerTimerActive() {
        return prefs.getBoolean(KEY_RINGER_TIMER_ACTIVE, false);
    }

    public void setRingerTimerActive(boolean isActive) {
        prefs.edit().putBoolean(KEY_RINGER_TIMER_ACTIVE, isActive).commit();
    }

    private boolean intializePrefsIfNeeded(Context context, int versionCode) {
        if (!prefs.getBoolean(KEY_PREF_INITIALIZED, false)) {
            Log.e(TAG, "init prefs");
            prefs.edit()
                    .putBoolean(KEY_PREF_ENABLED, true)
                    .putInt(KEY_VERSION, versionCode)
                    .putBoolean(KEY_PREF_AUTO_TIMER_ENABLED, true)
                    .putBoolean(KEY_PREF_ALWAYS_REVERT_TO_NORMAL, false)
                    .putString(KEY_PREF_DEFAULT_TIMER, "60")
                    .putLong(KEY_DEFAULT_TIMER, 60)
                    .putInt(KEY_PREVIOUS_RINGER_MODE, -1)
                    .putInt(KEY_CURRENT_RINGER_MODE, ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode())
                    .putBoolean(KEY_RINGER_TIMER_ACTIVE, false)
                    .putString(KEY_RINGER_TIMER, null)
                    .putBoolean(KEY_RINGER_CHANGED, false)
                    .putBoolean(KEY_PREF_INITIALIZED, true)
                    .commit();
            return true;
        }
        return false;
    }

    private boolean updatePrefsIfNeeded(Context context, int versionCode) {
        int storedVersionCode = prefs.getInt(KEY_VERSION, 0);
        if (storedVersionCode != versionCode) {
            SharedPreferences.Editor editor = prefs.edit();
            switch (storedVersionCode) {
                case 0:
                case 1:
                    editor.putBoolean(KEY_PREF_ALWAYS_REVERT_TO_NORMAL, false);
                case 2:
                    editor.putBoolean(KEY_RINGER_CHANGED, false);
                    break;
                default:
                    editor.commit();
                    return false;
            }
            editor.putInt(KEY_VERSION, versionCode);
            editor.commit();
            Log.d(TAG, "updated prefs to version" + versionCode);
            return true;
        }
        return true;
    }

    private void reInitializePrefs(Context context, int versionCode) {
        prefs.edit()
                .putBoolean(KEY_PREF_INITIALIZED, false)
                .commit();
        intializePrefsIfNeeded(context, versionCode);
    }

    public boolean getRingerChanged(){
        return prefs.getBoolean(KEY_RINGER_CHANGED, false);
    }

    public void setRingerChanged(boolean ringerChanged) {
        prefs.edit().putBoolean(KEY_RINGER_CHANGED, ringerChanged).commit();
    }
}
