/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
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
import android.text.Html;
import android.text.Spanned;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.util.PreferenceUtils;
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

/**
 * A {@link JobService} that runs every few hours to update the database and notify
 * the user of any changes.
 */
public class BlichFirebaseJobService extends JobService {

    private static final int NOTIFICATION_UPDATE_ID = 1;

    private AsyncTask<Void, Void, Void> mFetchBlichTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        PreferenceUtils.getInstance(getApplicationContext());
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

    private void notifyUser(Context context) {
        List<DatedLesson> notificationList = fetchNotificationList();
        if (!notificationList.isEmpty()) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context,
                    context.getString(R.string.notification_channel_schedule_id));
            NotificationCompat.InboxStyle inboxStyle = buildNotificationContent(notificationList, builder);

            Uri ringtone = Uri.parse(PreferenceUtils.getInstance().getString(R.string.pref_notification_sound_key));

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = builder
                    .setSmallIcon(R.drawable.ic_timetable_white_24dp)
                    .setContentTitle(context.getResources().getString(
                            R.string.notification_update_title))
                    .setSound(ringtone)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //noinspection ConstantConditions
            notificationManager.notify(NOTIFICATION_UPDATE_ID, notification);

        }
    }

    /**
     * Fetch all the changes in the schedule.
     *
     * @return a list of {@link DatedLesson}s.
     */
    private List<DatedLesson> fetchNotificationList() {
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
        RealmQuery<Change> changesQuery = RealmUtils.buildBaseQuery(
                realm,
                Change.class,
                minDate,
                maxDate)
                .sort("date");

        RealmQuery<Exam> examsQuery = RealmUtils.buildBaseQuery(
                realm,
                Exam.class,
                minDate,
                maxDate)
                .sort("date");

        RealmQuery<Event> eventsQuery = RealmUtils.buildBaseQuery(
                realm,
                Event.class,
                minDate,
                maxDate)
                .sort("date");

        boolean isFilterOn = PreferenceUtils.getInstance().getBoolean(R.string.pref_filter_toggle_key);
        if (isFilterOn) {
            RealmUtils.buildFilteredQuery(
                    changesQuery,
                    getBaseContext(),
                    Change.class);

            RealmUtils.buildFilteredQuery(
                    examsQuery,
                    getBaseContext(),
                    Exam.class);

            RealmUtils.buildFilteredQuery(
                    eventsQuery,
                    getBaseContext(),
                    Event.class);
        }

        List<DatedLesson> results = new ArrayList<>();
        results.addAll(changesQuery.findAll());
        results.addAll(examsQuery.findAll());
        results.addAll(eventsQuery.findAll());

        return results;
    }

    /**
     * Load the notification body with the changes.
     *
     * @param notificationList {@link DatedLesson}s to build content from.
     * @return notification body.
     */
    private NotificationCompat.InboxStyle buildNotificationContent(List<DatedLesson> notificationList,
                                                                    NotificationCompat.Builder builder) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        List<DatedLesson> todayNotificationChanges = new ArrayList<>();
        List<DatedLesson> tomorrowNotificationChanges = new ArrayList<>();
        for (DatedLesson lesson:
                notificationList) {
            Calendar date = Calendar.getInstance();
            date.setTime(lesson.getDate());
            int day = date.get(Calendar.DAY_OF_WEEK);
            if (today == day) todayNotificationChanges.add(lesson);
            else tomorrowNotificationChanges.add(lesson);
        }

        if (todayNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("היום:"));
            for (DatedLesson lesson :
                    todayNotificationChanges) {
                inboxStyle.addLine(lesson.buildName());
            }
        }


        if (tomorrowNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("מחר:"));
            for (DatedLesson lesson :
                    tomorrowNotificationChanges) {
                inboxStyle.addLine(lesson.buildName());
            }
        }

        //Save the number of changes in total;
        int changesNum = todayNotificationChanges.size() + tomorrowNotificationChanges.size();
        if (changesNum == 0) return null; //Stop

        String summery;
        if (changesNum == 1) summery = "ישנו שינוי אחד חדש";
        else summery = "ישנם " + changesNum + " שינויים חדשים";
        inboxStyle.setSummaryText(summery);

        builder.setContentText(summery);
        return inboxStyle;
    }

    /**
     * Convert the given text to a bold {@link Spanned}.
     *
     * @param text Text to apply the effect on.
     * @return The bold text
     */
    private Spanned getBoldText(String text) {
        return Html.fromHtml("<b> " + text + "</b>");
    }
}
