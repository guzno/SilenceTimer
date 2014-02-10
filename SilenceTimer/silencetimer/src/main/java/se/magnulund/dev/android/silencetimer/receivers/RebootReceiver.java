package se.magnulund.dev.android.silencetimer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import se.magnulund.dev.android.silencetimer.models.RingerTimer;
import se.magnulund.dev.android.silencetimer.preferences.Prefs;
import se.magnulund.dev.android.silencetimer.services.RingerTimerIntentService;

public class RebootReceiver extends BroadcastReceiver {
    private static final String TAG = "RebootReceiver";

    public RebootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Prefs prefs = Prefs.get(context);
            //Log.e(TAG, "handling reboot");
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int currentMode = audioManager.getRingerMode();
            prefs.updateRingerModes(currentMode);

            if (prefs.isRingerTimerActive()) {
                //Log.e(TAG, "timer active");
                RingerTimer ringerTimer = prefs.getRingerTimer();
                if (ringerTimer.getCurrentRingerMode() == currentMode) {
                    if (ringerTimer.isExpired()) {
                        //Log.e(TAG, "timer expired");
                        context.startService(RingerTimerIntentService.getRevertRingerModeIntent(context, ringerTimer.getTargetRingerMode()));
                    } else {
                        //Log.e(TAG, "timer restarting");
                        ringerTimer.recalculateDuration();
                        context.startService(RingerTimerIntentService.getStartRingerTimerIntent(context, ringerTimer));
                    }
                }
            }
        }
    }
}
