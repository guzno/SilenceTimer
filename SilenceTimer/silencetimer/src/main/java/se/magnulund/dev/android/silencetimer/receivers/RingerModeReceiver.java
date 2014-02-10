package se.magnulund.dev.android.silencetimer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import se.magnulund.dev.android.silencetimer.models.RingerTimer;
import se.magnulund.dev.android.silencetimer.notifications.NotificationSender;
import se.magnulund.dev.android.silencetimer.preferences.Prefs;
import se.magnulund.dev.android.silencetimer.services.RingerTimerIntentService;

public class RingerModeReceiver extends BroadcastReceiver {
    private static final String TAG = "RingerModeReceiver";

    public RingerModeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(intent.getAction())) {
            Prefs prefs = Prefs.get(context);
            if (prefs.isAppEnabled() && intent.hasExtra(AudioManager.EXTRA_RINGER_MODE)) {
                final int currentMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
                //Log.e(TAG, "current: "+currentMode);
                if (currentMode != -1) {
                    if (prefs.isRingerTimerActive()) {
                        RingerTimer activeTimer = prefs.getRingerTimer();
                        if (activeTimer.getCurrentRingerMode() != currentMode && activeTimer.isRunning()) {
                            activeTimer.stop(context);
                            prefs.clearRingerTimer();
                        }
                    }
                    NotificationSender.clearNotifications(context);
                    prefs.updateRingerModes(currentMode);
                    int previousMode;
                    if (prefs.alwaysRevertToNormal()) {
                        previousMode = AudioManager.RINGER_MODE_NORMAL;
                        //Log.e(TAG, "always revert: "+previousMode);
                    } else {
                        previousMode = prefs.getPreviousRingerMode();
                        //Log.e(TAG, "get prev: "+previousMode);
                    }
                    switch (currentMode) {
                        case AudioManager.RINGER_MODE_VIBRATE:
                        case AudioManager.RINGER_MODE_SILENT:
                            RingerTimer ringerTimer = prefs.getDefaultTimer(previousMode, currentMode);
                            //Log.e(TAG, "starting: c:"+ringerTimer.getCurrentRingerMode()+" p:"+ringerTimer.getTargetRingerMode());
                            if (prefs.isAutoModeEnabled() && !prefs.getRingerChanged()) {
                                context.startService(RingerTimerIntentService.getStartRingerTimerIntent(context, ringerTimer));
                            } else {
                                NotificationSender.sendRingerModeChangedNotification(context, ringerTimer);
                            }
                            break;
                        default:
                    }
                    prefs.setRingerChanged(false);
                }
            }
        }
    }
}