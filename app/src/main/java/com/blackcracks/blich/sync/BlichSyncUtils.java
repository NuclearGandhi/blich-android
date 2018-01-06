/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class BlichSyncUtils {

    private static final int SYNC_INTERVAL_HOURS = 6;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS/3;

    private static final String BLICH_SYNC_TAG = "blich_tag";

    private static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context,
                                                          FirebaseJobDispatcher dispatcher) {

        Job syncBlichJob = dispatcher.newJobBuilder()
                .setService(BlichFirebaseJobService.class)
                .setTag(BLICH_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS
                ))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncBlichJob);
    }

    synchronized public static void initialize(@NonNull Context context) {
        boolean is_notifications_on = Utilities.getPrefBoolean(context,
                Constants.Preferences.PREF_NOTIFICATION_TOGGLE_KEY);

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        if (is_notifications_on) {
            scheduleFirebaseJobDispatcherSync(context, dispatcher);
        } else {
            dispatcher.cancel(BLICH_SYNC_TAG);
        }
    }


    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, BlichSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
