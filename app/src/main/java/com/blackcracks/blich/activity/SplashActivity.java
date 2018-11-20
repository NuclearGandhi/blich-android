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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.sync.BlichSyncHelper;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.Utilities;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.crash.FirebaseCrash;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class SplashActivity extends BaseStatusBarActivity {

    private static final int SPLASH_SCREEN_DURATION = 250;

    private String mATEKey;
    private PreferenceUtils mPreferenceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mPreferenceUtils = PreferenceUtils.getInstance(this);
        RealmUtils.setUpRealm(this);
        applyTheme();
        migrateOldSettings();
        onUpdate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels();
        }

        configureCrashReporting();

        Timber.plant(new Timber.DebugTree());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_SCREEN_DURATION);
    }

    private void applyTheme() {
        setAutoStatusBarColor(true);
        configureTheme();

        mATEKey = Utilities.getATEKey(this);

        View rootView = findViewById(R.id.splash_root);

        int primaryColor = Config.primaryColor(this, mATEKey);
        rootView.setBackgroundColor(primaryColor);

        View logoView = findViewById(R.id.logo);
        int logoDrawableId = ATEUtil.isColorLight(primaryColor) ?
                R.drawable.logo_grey :
                R.drawable.logo_white;
        logoView.setBackgroundResource(logoDrawableId);

        ProgressBar loadingView = findViewById(R.id.loading);
        Drawable progressIntermediate = loadingView.getIndeterminateDrawable().mutate();
        progressIntermediate.setTint(Config.accentColor(this, mATEKey));
    }

    private void configureTheme() {
        // Default config
        if (!ATE.config(this, "light_theme").isConfigured(4)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.defaultLightPrimaryColor)
                    .accentColorRes(R.color.defaultLightAccentColor)
                    .statusBarColor(Color.TRANSPARENT)
                    .lightStatusBarMode(Config.LIGHT_STATUS_BAR_AUTO)
                    .navigationViewSelectedIconRes(R.color.defaultLightAccentColor)
                    .navigationViewSelectedTextRes(R.color.defaultLightAccentColor)
                    .commit();
        }

        if (!ATE.config(this, "dark_theme").isConfigured(4)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppTheme_Dark)
                    .primaryColorRes(R.color.defaultDarkPrimaryColor)
                    .accentColorRes(R.color.defaultDarkAccentColor)
                    .statusBarColor(Color.TRANSPARENT)
                    .lightStatusBarMode(Config.LIGHT_STATUS_BAR_AUTO)
                    .navigationViewSelectedIconRes(R.color.defaultDarkAccentColor)
                    .navigationViewSelectedTextRes(R.color.defaultDarkAccentColor)
                    .commit();
        }
    }

    private void migrateOldSettings() {
        //If using old user settings
        if (mPreferenceUtils.getInt(R.string.pref_user_class_group_key) == -1) {
            mPreferenceUtils.putBoolean(R.string.pref_is_first_launch_key, true);
            mPreferenceUtils.putInt(R.string.pref_user_class_group_key, 1);
        }
    }

    /**
     * Handle app updating.
     */
    private void onUpdate() {
        int oldVersion = mPreferenceUtils.getInt(R.string.pref_app_version_key);
        int newVersion = BuildConfig.VERSION_CODE;
        if (newVersion > oldVersion) {
            if (!mPreferenceUtils.getBoolean(R.string.pref_is_first_launch_key)) {
                Utilities.setClassGroupProperties(this);
                //Utilities.showChangelogDialog(getSupportFragmentManager());
            }

            if (oldVersion < 47) {
                Driver driver = new GooglePlayDriver(this);
                driver.cancel(BlichSyncHelper.BLICH_SYNC_TAG);
            }
            mPreferenceUtils.putInt(R.string.pref_app_version_key, newVersion);
        }
    }

    private void configureCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    @RequiresApi(api = 26)
    private void createNotificationChannels() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        @SuppressWarnings("ConstantConditions")
        NotificationChannel channel = notificationManager.getNotificationChannel(
                getString(R.string.notification_channel_schedule_id)
        );

        if (channel != null) {
            return;
        }

        NotificationChannel scheduleChannel = new NotificationChannel(
                getString(R.string.notification_channel_schedule_id),
                getString(R.string.notification_channel_schedule_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );

        scheduleChannel.setDescription(
                getString(R.string.notification_channel_schedule_description)
        );
        scheduleChannel.enableLights(true);
        scheduleChannel.setLightColor(Color.CYAN);

        notificationManager.createNotificationChannel(scheduleChannel);
    }
}
