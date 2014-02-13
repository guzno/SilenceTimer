package se.magnulund.dev.android.silencetimer.preferences;// Created by Gustav on 12/02/2014.

import android.content.Context;

public class Prefs extends BasePrefs {
    private static final String TAG = "Prefs";
    static final String KEY_PREF_SYNC_ENABLED = "pref_key_sync_enabled";

    private Prefs(Context context) {
        super(context);
    }

    public static Prefs get(Context context) {
        return new Prefs(context);
    }

    @Override
    boolean updatePrefs(int versionCode) {
        if (!super.updatePrefs(versionCode)) {
            return false;
        } else {
            switch (versionCode) {
                case 0:
                case 1:
                    break;
                default:
                    editor.commit();
                    return false;
            }
            editor.putInt(KEY_VERSION, versionCode);
            editor.commit();
            return true;
        }
    }

    @Override
    void initializePrefs(Context context, int versionCode) {
        super.initializePrefs(context, versionCode);
        editor.putBoolean(KEY_PREF_SYNC_ENABLED, false);
    }
}