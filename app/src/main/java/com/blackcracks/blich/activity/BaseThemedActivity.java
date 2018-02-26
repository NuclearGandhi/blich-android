package com.blackcracks.blich.activity;/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

import android.preference.PreferenceManager;

import com.afollestad.appthemeengine.ATEActivity;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseThemedActivity extends ATEActivity {

    @Override
    public final String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }
}