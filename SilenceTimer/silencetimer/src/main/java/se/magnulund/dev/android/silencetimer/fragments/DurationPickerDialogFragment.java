package se.magnulund.dev.android.silencetimer.fragments;// Created by Gustav on 05/02/2014.

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TabHost;
import android.widget.TimePicker;

import java.util.Calendar;

import se.magnulund.dev.android.silencetimer.R;
import se.magnulund.dev.android.silencetimer.models.RingerTimer;
import se.magnulund.dev.android.silencetimer.utils.DateTimeUtil;

public class DurationPickerDialogFragment extends DialogFragment {

    private static final String TAG = "DurationPickerDialogFragment";

    private static final String RINGER_TIMER = "ringer_timer";

    private static final String DURATION_TAB = "duration_tab";
    private static final String TIME_TAB = "time_tab";

    private DurationPickerListener durationPickerListener;

    public static DurationPickerDialogFragment newInstance(RingerTimer ringerTimer) {
        DurationPickerDialogFragment frag = new DurationPickerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(RingerTimer.RINGER_TIMER, ringerTimer);
        frag.setArguments(args);
        return frag;
    }

    public interface DurationPickerListener {
        void durationSet(RingerTimer ringerTimer);

        void dialogClosed();
    }

    @Override
    public void onAttach(Activity activity) {
        assert activity instanceof DurationPickerListener;
        durationPickerListener = (DurationPickerListener) activity;
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity mContext = getActivity();

        Bundle args = getArguments();

        if (args != null && mContext != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            LayoutInflater inflater = mContext.getLayoutInflater();

            final RingerTimer ringerTimer = args.getParcelable(RINGER_TIMER);

            View view = inflater.inflate(R.layout.fragment_duration_picker_dialog_view, null);

            if (view != null && ringerTimer != null) {

                final TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
                tabHost.setup();

                FrameLayout tabContent = tabHost.getTabContentView();

                assert tabContent != null;

                final LinearLayout durationPicker = (LinearLayout) tabContent.findViewById(R.id.duration_picker);

                final NumberPicker hourPicker = (NumberPicker) durationPicker.findViewById(R.id.hour_picker);
                hourPicker.setEnabled(true);
                hourPicker.setMaxValue(24);
                hourPicker.setMinValue(0);
                hourPicker.setValue(ringerTimer.getDurationHour());

                final NumberPicker minutePicker = (NumberPicker) durationPicker.findViewById(R.id.minute_picker);
                minutePicker.setEnabled(true);
                minutePicker.setMaxValue(59);
                minutePicker.setMinValue(0);
                minutePicker.setValue(ringerTimer.getDurationMinute());

                final Calendar now = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(ringerTimer.getStartTimestamp() + ringerTimer.getDuration());

                final TimePicker timePicker = (TimePicker) tabContent.findViewById(R.id.time_picker);
                timePicker.setIs24HourView(DateFormat.is24HourFormat(mContext));
                timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
                timePicker.setVisibility(View.GONE);

                TabHost.TabSpec durationTab = tabHost.newTabSpec(DURATION_TAB);
                durationTab.setContent(new TabHost.TabContentFactory() {
                    @Override
                    public LinearLayout createTabContent(String tag) {
                        return durationPicker;
                    }
                });
                durationTab.setIndicator(mContext.getString(R.string.dialog_tab_duration), getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                tabHost.addTab(durationTab);

                TabHost.TabSpec timeTab = tabHost.newTabSpec(TIME_TAB);
                timeTab.setContent(new TabHost.TabContentFactory() {
                    @Override
                    public TimePicker createTabContent(String tag) {
                        return timePicker;
                    }
                });
                timeTab.setIndicator(mContext.getString(R.string.dialog_tab_time), getResources().getDrawable(android.R.drawable.ic_menu_agenda));
                tabHost.addTab(timeTab);

                tabHost.setCurrentTabByTag(DURATION_TAB);

                String title;

                switch (ringerTimer.getCurrentRingerMode()) {
                    case AudioManager.RINGER_MODE_VIBRATE:
                        title = mContext.getString(R.string.vibration_mode);
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        title = mContext.getString(R.string.silent_mode);
                        break;
                    default:
                        title = "";
                }

                builder.setTitle(String.format(mContext.getString(R.string.dialog_set_duration), title))
                        .setView(view)
                        .setPositiveButton(mContext.getString(R.string.dialog_set_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                long duration;
                                String tab = tabHost.getCurrentTabTag();
                                if (tab != null) {
                                    long currentTime = now.getTimeInMillis();
                                    if (tab.equals(DURATION_TAB)) {
                                        duration = hourPicker.getValue() * DateTimeUtil.MILLIS_PER_HOUR + minutePicker.getValue() * DateTimeUtil.MILLIS_PER_MINUTE;
                                    } else {
                                        int hour = timePicker.getCurrentHour();
                                        int minute = timePicker.getCurrentMinute();
                                        Calendar timerEnd = Calendar.getInstance();
                                        if (now.get(Calendar.HOUR_OF_DAY) > hour || (now.get(Calendar.HOUR_OF_DAY) == hour && now.get(Calendar.MINUTE) < minute)){
                                            timerEnd.roll(Calendar.DATE, 1);
                                        }
                                        timerEnd.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                                        timerEnd.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                                        duration = timerEnd.getTimeInMillis() - currentTime;
                                    }
                                    ringerTimer.stop(mContext);
                                    ringerTimer.setDuration(duration);
                                    ringerTimer.setStartTimestamp(currentTime);
                                    durationPickerListener.durationSet(ringerTimer);
                                } else {
                                    Log.e(TAG, "No tab active!?!, duration not set");
                                    durationPickerListener.dialogClosed();
                                }
                            }
                        })
                        .setNegativeButton(mContext.getString(R.string.dialog_cancel_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                durationPickerListener.dialogClosed();
                            }
                        });

            }

            return builder.create();

        } else {
            return null;
        }
    }


}
