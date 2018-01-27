/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.PreferencesUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.Utilities;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;

public class BlichFirebaseJobService extends JobService {

    private static final int NOTIFICATION_UPDATE_ID = 1;

    private AsyncTask<Void, Void, Void> mFetchBlichTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mFetchBlichTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                RealmUtils.setUpRealm(getApplicationContext());
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                BlichSyncTask.syncBlich(context);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
                Utilities.updateWidget(getBaseContext());
            }
        };
        mFetchBlichTask.execute();

        notifyUser(getBaseContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mFetchBlichTask != null) {
            mFetchBlichTask.cancel(true);
        }
        return true;
    }

    private List<Change> fetchNotificationList() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date minDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_WEEK, 1);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        Date maxDate = calendar.getTime();

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Change> query = RealmUtils.buildBaseChangeQuery(
                realm,
                Change.class,
                minDate,
                maxDate
        );

        RealmUtils.buildFilteredQuery(
                query,
                getBaseContext(),
                Change.class
        );

        query.sort("date");

        return query.findAll();
    }

    private void notifyUser(Context context) {
        List<Change> notificationList = fetchNotificationList();
        if (!notificationList.isEmpty()) {

            NotificationCompat.InboxStyle inboxStyle = buildNotificationContent(notificationList);

            int intKey = Constants.Preferences.PREF_NOTIFICATION_SOUND_KEY;
            String prefKey = Constants.Preferences.getKey(context, intKey);
            String prefDefault = (String) Constants.Preferences.getDefault(context, intKey);
            Uri ringtone = Uri.parse(PreferencesUtils
                    .getString(context,
                            prefKey,
                            prefDefault,
                            true));

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(
                    context,
                    context.getString(R.string.notification_channel_schedule_id))
                    .setSmallIcon(R.drawable.ic_timetable_white_24dp)
                    .setContentTitle(context.getResources().getString(
                            R.string.notification_update_title))
                    .setSound(ringtone)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_UPDATE_ID, notification);

        }
    }

    private NotificationCompat.InboxStyle buildNotificationContent(List<Change> notificationList) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        List<Change> todayNotificationChanges = new ArrayList<>();
        List<Change> tomorrowNotificationChanges = new ArrayList<>();
        for (Change change:
                notificationList) {
            Calendar date = Calendar.getInstance();
            date.setTime(change.getDate());
            int day = date.get(Calendar.DAY_OF_WEEK);
            if (today == day) todayNotificationChanges.add(change);
            else tomorrowNotificationChanges.add(change);
        }

        if (todayNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("היום:"));
            for (Change change :
                    todayNotificationChanges) {
                inboxStyle.addLine(change.buildSubject());
            }
        }


        if (tomorrowNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("מחר:"));
            for (Change change :
                    tomorrowNotificationChanges) {
                inboxStyle.addLine(change.buildSubject());
            }
        }

        //Save the number of changes in total;
        int changesNum = todayNotificationChanges.size() + tomorrowNotificationChanges.size();
        if (changesNum == 0) return null; //Stop

        String summery;
        if (changesNum == 1) summery = "ישנו שינוי אחד חדש";
        else summery = "ישנם " + changesNum + " שינויים חדשים";
        inboxStyle.setSummaryText(summery);

        return inboxStyle;
    }

    private Spanned getBoldText(String text) {
        return Html.fromHtml("<b> " + text + "</b>");
    }
}
