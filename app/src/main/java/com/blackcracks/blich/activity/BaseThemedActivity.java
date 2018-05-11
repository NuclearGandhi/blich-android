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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.view.View;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATECollapsingTbCustomizer;
import com.blackcracks.blich.util.Utilities;

import java.util.Locale;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseThemedActivity extends ATEActivity implements
        ATECollapsingTbCustomizer {

    private boolean mAutoStatusBarColor = true;

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
    protected void onStart() {
        super.onStart();
        if (mAutoStatusBarColor) getWindow().setStatusBarColor(Config.primaryColorDark(this, getATEKey()));
    }

    @Override
    public final String getATEKey() {
        return Utilities.getATEKey(this);
    }

    public void setAllowDrawBehindStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void setAutoStatusBarColor(boolean autoStatusBarColor) {
        mAutoStatusBarColor = autoStatusBarColor;
    }

    @Override
    public int getCollapsedTintColor() {
        return Config.getToolbarTitleColor(this, null, getATEKey());
    }

    @Override
    public int getExpandedTintColor() {
        return Config.getToolbarTitleColor(this, null, getATEKey());
    }
}