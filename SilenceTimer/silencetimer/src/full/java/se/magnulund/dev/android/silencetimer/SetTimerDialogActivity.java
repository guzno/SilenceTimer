package se.magnulund.dev.android.silencetimer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import se.magnulund.dev.android.silencetimer.fragments.DurationPickerDialogFragment;
import se.magnulund.dev.android.silencetimer.fragments.DurationPickerDialogFragmentBase;
import se.magnulund.dev.android.silencetimer.models.RingerTimer;
import se.magnulund.dev.android.silencetimer.services.RingerTimerIntentService;

public class SetTimerDialogActivity extends Activity implements DurationPickerDialogFragmentBase.DurationPickerListener {

    private DurationPickerDialogFragment durationPickerDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_dialog);

        Bundle extras = getIntent().getExtras();

        this.setFinishOnTouchOutside(true);

        if (extras != null) {
            RingerTimer ringerTimer = extras.getParcelable(RingerTimer.RINGER_TIMER);
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            durationPickerDialogFragment = (DurationPickerDialogFragment) DurationPickerDialogFragment.newInstance(ringerTimer);

            durationPickerDialogFragment.show(ft, "dialog");
        }
    }

    @Override
    public void durationSet(RingerTimer ringerTimer) {
        durationPickerDialogFragment.dismiss();
        Intent intent = RingerTimerIntentService.getStartRingerTimerIntent(this, ringerTimer);
        startService(intent);
        finish();
    }

    @Override
    public void dialogClosed() {
        durationPickerDialogFragment.dismiss();
        finish();
    }
}
