/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.activity;/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.afollestad.appthemeengine.ATEActivity;
import com.blackcracks.blich.util.Utilities;

import java.util.Locale;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseThemedActivity extends ATEActivity {

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocaleToHebrew(this);
    }

        /**
         * Change {@link Locale} to Hebrew, right to left.
         */
    private void setLocaleToHebrew(Context context) {
        //Change locale to hebrew
        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.getApplicationContext().createConfigurationContext(config);
    }

    @Override
    public final String getATEKey() {
        return Utilities.getATEKey(this);
    }
}