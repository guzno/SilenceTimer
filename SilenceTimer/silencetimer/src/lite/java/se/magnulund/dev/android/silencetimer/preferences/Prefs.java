package se.magnulund.dev.android.silencetimer.preferences;// Created by Gustav on 12/02/2014.

import android.content.Context;

public class Prefs extends BasePrefs {
    private static final String TAG = "Prefs";

    private Prefs(Context context) {
        super(context);
    }

    public static Prefs get(Context context){
        return new Prefs(context);
    }
}