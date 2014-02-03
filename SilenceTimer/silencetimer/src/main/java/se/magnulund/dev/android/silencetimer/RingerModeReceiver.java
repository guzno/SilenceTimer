package se.magnulund.dev.android.silencetimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

public class RingerModeReceiver extends BroadcastReceiver {
    public RingerModeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(AudioManager.EXTRA_RINGER_MODE)) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            switch (intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1)) {
                case AudioManager.RINGER_MODE_NORMAL:
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
                default:
            }
        }
    }
}