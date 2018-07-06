package com.mbcode64.android.thirdeye;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class myPref {

    public int history, video, sensitivity, gif;
    public boolean email;

    public myPref(Context c) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);

        history = 7;//Integer.getInteger(sharedPref.getString("pref_history", "7"));
        video = 10;//Integer.getInteger(sharedPref.getString("pref_vidLength", "10"));
        gif = 10;//Integer.getInteger(sharedPref.getString("pref_gifLength", "10"));
        email = sharedPref.getBoolean("pref_email", true);

        switch (sharedPref.getString("pref_sensitivity", "Medium")) {
            case "Low":
                sensitivity = 80;
                break;
            case "High":
                sensitivity = 40;
                break;
            default:
                sensitivity = 60;
        }
    }
}
