package se.magnulund.dev.android.silencetimer;

import android.os.Bundle;

import se.magnulund.dev.android.silencetimer.fragments.SettingsFragment;

public class MainActivity extends MainActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }
}
