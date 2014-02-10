package se.magnulund.dev.android.silencetimer.notifications;// Created by Gustav on 04/02/2014.

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import se.magnulund.dev.android.silencetimer.R;
import se.magnulund.dev.android.silencetimer.SetTimerDialogActivity;
import se.magnulund.dev.android.silencetimer.models.RingerTimer;
import se.magnulund.dev.android.silencetimer.services.RingerTimerIntentService;
import se.magnulund.dev.android.silencetimer.utils.DateTimeUtil;

public class NotificationSender {
    private static final String TAG = "NotificationSender";

    public static final int TIMER_STARTED_NOTIFICATION_ID = 1;
    public static final int RINGER_MODE_CHANGED_NOTIFICATION_ID = 2;

    private static void sendNotification(Context context, Notification notification, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void sendRingerModeChangedNotification(Context context, RingerTimer ringerTimer) {

        String title;

        switch (ringerTimer.getCurrentRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                title = context.getString(R.string.notification_title_silent);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                title = context.getString(R.string.notification_title_vibrate);
                break;
            default:
                return;
        }

        String text = String.format(context.getString(R.string.notification_set_duration_content), DateTimeUtil.getDurationString(ringerTimer.getDuration()));

        Intent defaultIntent = RingerTimerIntentService.getStartRingerTimerIntent(context, ringerTimer);

        PendingIntent startDefaultTimerIntent = PendingIntent.getService(context, 0, defaultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent setTimerDialogIntent = getSetTimerDialogIntent(context, ringerTimer);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(startDefaultTimerIntent)
                .setSmallIcon(R.drawable.ic_stat_ringer_change)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_action_set, context.getString(R.string.notification_set_duration_button), setTimerDialogIntent).build();

        sendNotification(context, notification, RINGER_MODE_CHANGED_NOTIFICATION_ID);
    }

    public static void sendRingerTimerStartedNotification(Context context, RingerTimer ringerTimer) {

        Intent abortIntent = RingerTimerIntentService.getAbortRingerRevertIntent(context, ringerTimer);

        PendingIntent abortTimerIntent = PendingIntent.getService(context, 0, abortIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title;

        switch (ringerTimer.getCurrentRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                title = String.format(context.getString(R.string.notification_title_silenced_until), DateTimeUtil.getExpirryTimeString(context, ringerTimer));
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                title = String.format(context.getString(R.string.notification_title_vibrate_until), DateTimeUtil.getExpirryTimeString(context, ringerTimer));
                break;
            default:
                title = "";
        }

        String text = context.getString(R.string.notification_content_change_duration);

        PendingIntent setTimerDialogIntent = getSetTimerDialogIntent(context, ringerTimer);

        PendingIntent increaseTimerDurationIntent = getIncreaseTimerDurationIntent(context, ringerTimer);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(setTimerDialogIntent)
                .setSmallIcon(R.drawable.ic_stat_ringer_change)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_cancel, context.getString(R.string.notification_cancel_button), abortTimerIntent)
                .addAction(R.drawable.ic_action_increase, context.getString(R.string.notification_change_duration_button), increaseTimerDurationIntent).build();

        sendNotification(context, notification, TIMER_STARTED_NOTIFICATION_ID);
    }

    private static PendingIntent getIncreaseTimerDurationIntent(Context context, RingerTimer ringerTimer) {
        ringerTimer.setDuration(ringerTimer.getDuration()+DateTimeUtil.MILLIS_PER_HOUR);
        Intent intent = RingerTimerIntentService.getStartRingerTimerIntent(context, ringerTimer);

        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getSetTimerDialogIntent(Context context, RingerTimer ringerTimer) {

        Intent intent = new Intent(context, SetTimerDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(RingerTimer.RINGER_TIMER, ringerTimer);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void clearNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}
