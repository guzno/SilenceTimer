package se.magnulund.dev.android.silencetimer.fragments;// Created by Gustav on 05/02/2014.

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

public class DurationPickerDialogFragment extends DurationPickerDialogFragmentBase {

    private static final String TAG = "DurationPickerDialogFragmentBase";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.e(TAG, "I am a subclass of DurationPickerDialogFragmentBase!");
        return super.onCreateDialog(savedInstanceState);
    }
}
