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
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.Utilities;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

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
                Utilities.Realm.setUpRealm(getApplicationContext());
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

    private List<Hour> fetchNotificationList() {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int tomorrow = today + 1;
        if (tomorrow == 8) tomorrow = 1;

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Hour> results = realm.where(Hour.class)
                .beginGroup()
                    .equalTo("day", today)
                    .or()
                    .equalTo("day", tomorrow)
                .endGroup()
                .and()
                .notEqualTo("lessons.changeType", Database.TYPE_NORMAL)
                .findAll();

        return results;
    }

    private void notifyUser(Context context) {
        List<Hour> notificationList = fetchNotificationList();
        if (!notificationList.isEmpty()) {

            NotificationCompat.InboxStyle inboxStyle = buildNotificationContent(notificationList);

            int intKey = Constants.Preferences.PREF_NOTIFICATION_SOUND_KEY;
            String prefKey = Constants.Preferences.getKey(context, intKey);
            String prefDefault = (String) Constants.Preferences.getDefault(context, intKey);
            Uri ringtone = Uri.parse(Utilities
                    .getPrefString(context,
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

    private NotificationCompat.InboxStyle buildNotificationContent(List<Hour> notificationList) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        List<Lesson> todayNotificationLessons = new ArrayList<>();
        List<Lesson> tomorrowNotificationLessons = new ArrayList<>();
        for (Hour hour :
                notificationList) {
            List<Lesson> lessons = hour.getLessons();
            for (Lesson lesson :
                    lessons) {
                if (!lesson.getChangeType().equals(Database.TYPE_NORMAL)) {
                    if (hour.getDay() == day)
                        todayNotificationLessons.add(lesson);
                    else {
                        tomorrowNotificationLessons.add(lesson);
                    }
                }
            }
        }

        if (todayNotificationLessons.size() != 0) {
            inboxStyle.addLine(getBoldText("היום:"));
            for (Lesson lesson :
                    todayNotificationLessons) {
                inboxStyle.addLine(lesson.getSubject());
            }
        }


        if (tomorrowNotificationLessons.size() != 0) {
            inboxStyle.addLine(getBoldText("מחר:"));
            for (Lesson lesson :
                    tomorrowNotificationLessons) {
                inboxStyle.addLine(lesson.getSubject());
            }
        }

        //Save the number of changes in total;
        int changesNum = todayNotificationLessons.size() + tomorrowNotificationLessons.size();
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
