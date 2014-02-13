package se.magnulund.dev.android.silencetimer.fragments;// Created by Gustav on 12/02/2014.

import android.os.Bundle;
import android.preference.PreferenceManager;

import se.magnulund.dev.android.silencetimer.R;

public class SettingsFragment extends SettingsFragmentBase {
    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getActivity() != null;

        addPreferencesFromResource(R.xml.full_preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
    }
}
