/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichData;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.util.ClassUtils;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.PreferencesUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class BlichSyncUtils {

    //BlichData
    private static final String BLICH_BASE_URI =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/ApiHandler.ashx";

    private static final String PARAM_SID = "sid";
    private static final String PARAM_API_KEY = "token";
    private static final String PARAM_COMMAND = "cmd";
    private static final String PARAM_CLASS_ID = "clsid";


    private static final int BLICH_ID = 540211;
    static final String COMMAND_CLASSES = "classes";
    static final String COMMAND_SCHEDULE = "schedule";
    static final String COMMAND_EXAMS = "exams";
    static final String COMMAND_EVENTS = "events";
    static final String COMMAND_CHANGES = "changes";

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
        boolean is_notifications_on = PreferencesUtils.getBoolean(context,
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

    public static void loadDataIntoRealm(BlichData blichData) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        //Delete old data
        RealmResults<Hour> hours = realm.where(Hour.class)
                .findAll();
        hours.deleteAllFromRealm();

        RealmResults<Change> changes = realm.where(Change.class)
                .findAll();
        changes.deleteAllFromRealm();

        RealmResults<Event> events = realm.where(Event.class)
                .findAll();
        events.deleteAllFromRealm();

        RealmResults<Exam> exams = realm.where(Exam.class)
                .findAll();
        exams.deleteAllFromRealm();

        //Insert new data
        realm.insert(blichData);
        realm.commitTransaction();
        realm.close();
    }

    public static int getClassValue(Context context)
            throws BlichFetchException {
        String currentClass = ClassUtils.getCurrentClass(context);
        String selection;
        String[] selectionArgs;

        if (currentClass.contains("'")) { //Normal class syntax
            selection = BlichContract.ClassEntry.COL_GRADE + " = ? AND " +
                    BlichContract.ClassEntry.COL_GRADE_INDEX + " = ?";
            selectionArgs = currentClass.split("'");
        } else { //Abnormal class syntax
            selection = BlichContract.ClassEntry.COL_GRADE + " = ?";
            selectionArgs = new String[]{currentClass};
        }


        Cursor cursor = context.getContentResolver().query(
                BlichContract.ClassEntry.CONTENT_URI,
                new String[]{BlichContract.ClassEntry.COL_CLASS_INDEX},
                selection,
                selectionArgs,
                null);

        int classValue;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                classValue = cursor.getInt(0);
            } else {
                throw new BlichFetchException("Can't get the user's class. " +
                        "Did the user configure his class?");
            }
        } else {
            throw new NullPointerException("Queried cursor is null");
        }

        cursor.close();
        return classValue;
    }

    public static URL buildUrlFromCommand(Context context, String command)
            throws BlichFetchException {
        int classValue = getClassValue(context);

        Uri scheduleUri = Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_CLASS_ID, String.valueOf(classValue))
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();

        if (BuildConfig.DEBUG) Timber.d("Building URI: %s", scheduleUri.toString());

        try {
            return new URL(scheduleUri.toString());
        } catch (MalformedURLException e) {
            Timber.e(e);
            return null;
        }
    }

    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection scheduleConnection = (HttpURLConnection) url.openConnection();

        InputStream in = scheduleConnection.getInputStream();

        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\A");

        boolean hasInput = scanner.hasNext();
        String response = null;
        if (hasInput) {
            response = scanner.next();
        }
        scanner.close();
        scheduleConnection.disconnect();

        return response;
    }

    public static class BlichFetchException extends Exception {
        public BlichFetchException(String message) {
            super(message);
        }
    }
}
