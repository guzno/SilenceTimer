package se.magnulund.dev.android.silencetimer.preferences;// Created by Gustav on 12/02/2014.

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class LongListPreference extends ListPreference{
    private static final String TAG = "LongListPreference";

    public LongListPreference(Context context) {
        super(context);
    }

    public LongListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected String getPersistedString(String defaultValue) {
        return String.valueOf(getPersistedLong(-1));
    }

    @Override
    protected boolean persistString(String value) {
        return persistLong(Long.valueOf(value));
    }
}
