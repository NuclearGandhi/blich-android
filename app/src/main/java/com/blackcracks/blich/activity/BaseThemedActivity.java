/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.activity;

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