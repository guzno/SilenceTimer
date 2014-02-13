package se.magnulund.dev.android.silencetimer.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.widget.Toast;

import se.magnulund.dev.android.silencetimer.R;
import se.magnulund.dev.android.silencetimer.models.RingerTimer;
import se.magnulund.dev.android.silencetimer.notifications.NotificationSender;
import se.magnulund.dev.android.silencetimer.preferences.Prefs;
import se.magnulund.dev.android.silencetimer.utils.DateTimeUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class RingerTimerIntentService extends IntentService {

    private static final String TAG = "RingerTimerIntentService";

    private static final String ACTION_START_RINGER_TIMER = "se.magnulund.dev.android.silencetimer.services.action.START_RINGER_TIMER";
    private static final String ACTION_REVERT_RINGER_MODE = "se.magnulund.dev.android.silencetimer.services.action.REVERT_RINGER_MODE";
    private static final String ACTION_ABORT_RINGER_REVERT = "se.magnulund.dev.android.silencetimer.services.action.ABORT_RINGER_REVERT";

    private static final String EXTRA_RINGER_TIMER = "se.magnulund.dev.android.silencetimer.services.extra.RINGER_TIMER";
    private static final String EXTRA_RINGER_MODE = "se.magnulund.dev.android.silencetimer.services.extra.RINGER_MODE";

    private Prefs prefs;

    public static Intent getStartRingerTimerIntent(Context context, RingerTimer ringerTimer) {
        Intent intent = new Intent(context, RingerTimerIntentService.class);
        intent.setAction(ACTION_START_RINGER_TIMER);
        intent.putExtra(EXTRA_RINGER_TIMER, ringerTimer);
        return intent;
    }

    public static Intent getRevertRingerModeIntent(Context context, int targetRingerMode) {
        Intent intent = new Intent(context, RingerTimerIntentService.class);
        intent.setAction(ACTION_REVERT_RINGER_MODE);
        intent.putExtra(EXTRA_RINGER_MODE, targetRingerMode);
        return intent;
    }

    public static Intent getAbortRingerRevertIntent(Context context, RingerTimer ringerTimer) {
        Intent intent = new Intent(context, RingerTimerIntentService.class);
        intent.setAction(ACTION_ABORT_RINGER_REVERT);
        intent.putExtra(EXTRA_RINGER_TIMER, ringerTimer);
        return intent;
    }

    public RingerTimerIntentService() {
        super("RingerTimerIntentService");
    }

    private Context mContext;
    private Handler toastHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        toastHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        prefs = Prefs.get(this);
        if (ACTION_START_RINGER_TIMER.equals(action)) {
            RingerTimer ringerTimer = intent.getParcelableExtra(EXTRA_RINGER_TIMER);
            handleActionStartRingerTimer(ringerTimer);
        } else if (ACTION_REVERT_RINGER_MODE.equals(action)) {
            int ringerMode = intent.getIntExtra(EXTRA_RINGER_MODE, -1);
            handleActionRevertRingerMode(ringerMode);
        } else if (ACTION_ABORT_RINGER_REVERT.equals(action)) {
            RingerTimer ringerTimer = intent.getParcelableExtra(EXTRA_RINGER_TIMER);
            handleActionAbortRingerRevert(ringerTimer);
        }
    }

    private void handleActionAbortRingerRevert(RingerTimer ringerTimer) {
        ringerTimer.stop(this);
        NotificationSender.clearNotification(this, NotificationSender.TIMER_STARTED_NOTIFICATION_ID);
        prefs.clearRingerTimer();
        makeCancelledToast();
    }

    private void handleActionRevertRingerMode(int ringerMode) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //Log.e(TAG, "reverting to "+ringerMode);
        prefs.setRingerChanged(true);

        audioManager.setRingerMode(ringerMode);

        NotificationSender.clearNotifications(this);

        prefs.clearRingerTimer();
    }

    private void handleActionStartRingerTimer(RingerTimer ringerTimer) {
        NotificationSender.clearNotification(this, NotificationSender.RINGER_MODE_CHANGED_NOTIFICATION_ID);
        if (ringerTimer.isRunning()){
            ringerTimer.stop(this);
        }
        ringerTimer.start(this);
        prefs.setRingerTimer(ringerTimer);
        NotificationSender.sendRingerTimerStartedNotification(this, ringerTimer);
        makeStartToast(ringerTimer);
    }

    private void makeStartToast(RingerTimer ringerTimer){
        String toastText;
        String expiryTime = DateTimeUtil.getExpirryTimeString(this, ringerTimer);
        switch (ringerTimer.getCurrentRingerMode()){
            case AudioManager.RINGER_MODE_SILENT:
                toastText = String.format(getString(R.string.toast_silence_timer_started), expiryTime);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                toastText = String.format(getString(R.string.toast_vibrate_timer_started), expiryTime);
                break;
            default:
                toastText = String.format(getString(R.string.toast_unknown_timer_started), expiryTime);
        }
        toastHandler.post(new DisplayToast(toastText));
    }

    private void makeCancelledToast(){
        toastHandler.post(new DisplayToast(getString(R.string.toast_cancelled)));
    }

    private class DisplayToast implements Runnable{
        final String mText;

        public DisplayToast(String text){
            mText = text;
        }

        public void run(){
            Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
        }
    }
}
