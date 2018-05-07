/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.sync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichData;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.receiver.BootReceiver;
import com.blackcracks.blich.receiver.ScheduleAlarmReceiver;
import com.blackcracks.blich.util.PreferenceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A utility class for syncing.
 */
public class BlichSync {

    //BlichData
    private static final String BLICH_BASE_URI =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/ApiHandler.ashx";

    private static final String PARAM_SID = "sid";
    private static final String PARAM_API_KEY = "token";
    private static final String PARAM_COMMAND = "cmd";
    private static final String PARAM_CLASS_ID = "clsid";

    private static final int BLICH_ID = 540211;

    @Retention(SOURCE)
    @StringDef({COMMAND_CHANGES, COMMAND_EVENTS, COMMAND_EXAMS, COMMAND_SCHEDULE, COMMAND_CLASSES})
    @interface FetchCommand{}

    static final String COMMAND_CLASSES = "classes";
    static final String COMMAND_SCHEDULE = "schedule";
    static final String COMMAND_EXAMS = "exams";
    static final String COMMAND_EVENTS = "events";
    static final String COMMAND_CHANGES = "changes";

    private static final int REQUEST_CODE_EVENING_ALARM = 0;
    private static final int REQUEST_CODE_MORNING_ALARM = 1;

    public static final String BLICH_SYNC_TAG = "blich_tag";

    /**
     * Start or cancel the periodic sync.
     */
    public static void initializePeriodicSync(@NonNull Context context) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, ScheduleAlarmReceiver.class);
        PendingIntent eveningPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_EVENING_ALARM,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent morningPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MORNING_ALARM,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        boolean isNotificationsOn = PreferenceUtils.getInstance().getBoolean(R.string.pref_notification_toggle_key);
        if (isNotificationsOn) {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            if (hourOfDay > 21 || (hourOfDay == 21 && minute > 30))
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 21);
            calendar.set(Calendar.MINUTE, 30);

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    eveningPendingIntent
            );

            if (BuildConfig.DEBUG) {
                Timber.d("Evening sync starting on %s", calendar);
            }

            calendar = Calendar.getInstance();

            if (hourOfDay >= 7)
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 0);

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    morningPendingIntent
            );

            if (BuildConfig.DEBUG) {
                Timber.d("Morning sync starting on %s", calendar);
            }
        } else {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            alarmManager.cancel(eveningPendingIntent);
            alarmManager.cancel(morningPendingIntent);
        }

        if (BuildConfig.DEBUG) {
            Timber.d(isNotificationsOn ?
                        "Periodic sync is enabled" :
                        "Periodic sync is disabled");
        }
    }

    /**
     * Begin sync immediately.
     */
    public static void startImmediateSync(@NonNull Context context) {
        Intent intentToSyncImmediately = new Intent(context, BlichSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }

    /**
     * Insert the given data into realm. Deletes all the old data.
     *
     * @param blichData data to insert.
     */
    static void loadDataIntoRealm(BlichData blichData) {
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

    /**
     * Build a URL to Shahaf's servers.
     *
     * @param command a {@link FetchCommand}.
     * @return a {@link URL}.
     */
    static URL buildUrlFromCommand(Context context, @FetchCommand String command) {
        int classValue = PreferenceUtils.getInstance().getInt(R.string.pref_user_class_group_key);

        Uri scheduleUri = Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_CLASS_ID, String.valueOf(classValue))
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();

        return buildURLFromUri(scheduleUri);
    }

    /**
     * Build a URI without {@link #PARAM_CLASS_ID} parameter.
     *
     * @param command a {@link FetchCommand}.
     * @return a {@link Uri}.
     */
    @SuppressWarnings("SameParameterValue")
    static Uri buildBaseUriFromCommand(@FetchCommand String command) {

        return Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();
    }

    /**
     * Convert a URI into a URL.
     *
     * @param uri the {@link Uri} to convert.
     * @return a {@link URL}.
     */
    static URL buildURLFromUri(Uri uri) {
        try {

            if (BuildConfig.DEBUG) {
                Timber.d("Building URI: %s", uri.toString());
            }
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            Timber.e(e);
            return null;
        }
    }

    /**
     * Connect to the given url, and return its response.
     *
     * @param url {@link URL} to connect to.
     * @return server response.
     */
    static String getResponseFromUrl(URL url) throws IOException {
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
}
