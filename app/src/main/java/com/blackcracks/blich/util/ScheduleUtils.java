/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.raw.RawModifier;
import com.blackcracks.blich.data.schedule.Lesson;
import com.blackcracks.blich.data.schedule.Modifier;
import com.blackcracks.blich.data.schedule.Period;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * A class containing utility methods for schedule.
 */
public class ScheduleUtils {

    private static final int NOTIFICATION_UPDATE_ID = 1;

    /**
     * Get the wanted day of the week based on current time.
     *
     * @return day of the week.
     */
    public static int getWantedDayOfTheWeek() {
        Calendar instance = Calendar.getInstance();
        int hour = instance.get(Calendar.HOUR_OF_DAY);

        int daysToAdd = 0;
        if (hour >= 18) {
            daysToAdd++;

            if (instance.get(Calendar.DAY_OF_WEEK) == 6)
                daysToAdd++;
        }

        instance.add(Calendar.DAY_OF_WEEK, daysToAdd);
        return instance.get(Calendar.DAY_OF_WEEK);
    }

    public static int getWantedWeekOffset(@Nullable Calendar instance) {
        if (instance == null)
            instance = Calendar.getInstance();

        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int day = instance.get(Calendar.DAY_OF_WEEK);

        if ((hour >= 18 && day == 6) || day == 7) {
            return 1;
        }
        return 0;
    }

    public static List<Period> fetchScheduleData(Realm realm, int day) {
        boolean isFilterOn = PreferenceUtils.getInstance().getBoolean(R.string.pref_filter_toggle_key);
        List<Period> data;
        if (isFilterOn) {
            List<Lesson> lessons;

            lessons = realm.copyFromRealm(
                    buildFilteredQuery(realm, day)
                            .findAll()
            );

            data = RealmUtils.convertLessonListToPeriodList(lessons, day);
        } else {
            data = buildQuery(realm, day)
                    .findAll();

            data = realm.copyFromRealm(data);
        }
        return data;
    }

    public static List<Period> fetchScheduleDataAsync(
            Realm realm,
            final int day,
            final OnRealmAsyncFinishedListener listener) {
        boolean isFilterOn = PreferenceUtils.getInstance().getBoolean(R.string.pref_filter_toggle_key);
        final List<Period> data;
        if (isFilterOn) {
            RealmResults<Lesson> lessons;

            lessons =
                    buildFilteredQuery(realm, day)
                            .findAllAsync();

            lessons.addChangeListener(new RealmChangeListener<RealmResults<Lesson>>() {
                @Override
                public void onChange(@NonNull RealmResults<Lesson> lessons) {
                    listener.onAsyncFinished(RealmUtils.convertLessonListToPeriodList(lessons, day));
                }
            });

            data = RealmUtils.convertLessonListToPeriodList(lessons, day);
        } else {
            RealmResults<Period> periods = buildQuery(realm, day)
                    .findAllAsync();

            periods.addChangeListener(new RealmChangeListener<RealmResults<Period>>() {
                @Override
                public void onChange(RealmResults<Period> periods) {
                    listener.onAsyncFinished(periods);
                }
            });

            data = periods;
        }
        return data;
    }

    private static RealmQuery<Lesson> buildFilteredQuery(
            Realm realm,
            int day) {

        String teacherFilter = PreferenceUtils.getInstance().getString(R.string.pref_filter_select_key);
        String[] teacherSubjects = teacherFilter.split(";");

        RealmQuery<Lesson> query = realm.where(Lesson.class);
        query.equalTo("owners.day", day)
                .and()
                .beginGroup()
                    .beginGroup()
                    .isNull("teacher")
                    .and()
                    .isNull("subject")
                    .endGroup();


        for (String teacherSubject :
                teacherSubjects) {
            if (teacherSubject.equals("")) break;

            String[] arr = teacherSubject.split(",");
            String teacher = arr[0];
            String subject = arr[1];

            addTeacherSubjectFilter(query, teacher, subject);
        }

        query.endGroup();
        return query;
    }

    private static RealmQuery<Period> buildQuery(
            Realm realm,
            int day) {
        return realm.where(Period.class)
                .equalTo("day", day)
                .sort("periodNum");
    }

    private static void addTeacherSubjectFilter(
            RealmQuery<Lesson> query,
            String teacher,
            String subject) {
        query.or()
                .beginGroup()
                .equalTo("teacher", teacher)
                .and()
                .equalTo("subject", subject)
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("modifier.newTeacher", teacher)
                .endGroup();
    }

    public interface OnRealmAsyncFinishedListener {
        void onAsyncFinished(List<Period> data);
    }

    public static void notifyUser(Context context) {
        Realm realm = Realm.getDefaultInstance();
        List<Modifier> notificationList = fetchNotificationList(realm);
        if (!notificationList.isEmpty()) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context,
                    context.getString(R.string.notification_channel_schedule_id));
            NotificationCompat.InboxStyle inboxStyle = buildNotificationContent(notificationList, builder);

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
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //noinspection ConstantConditions
            notificationManager.notify(NOTIFICATION_UPDATE_ID, notification);

        }
        realm.close();
    }

    /**
     * Fetch all the changes in the schedule.
     *
     * @return a list of {@link RawModifier}s.
     */
    private static List<Modifier> fetchNotificationList(Realm realm) {
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

        return realm.where(Modifier.class)
                .between("date", minDate, maxDate)
                .sort("date")
                .findAll();
    }

    /**
     * Load the notification body with the changes.
     *
     * @param notificationList {@link RawModifier}s to build content from.
     * @return notification body.
     */
    private static NotificationCompat.InboxStyle buildNotificationContent(List<Modifier> notificationList,
                                                                          NotificationCompat.Builder builder) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        List<Modifier> todayNotificationChanges = new ArrayList<>();
        List<Modifier> tomorrowNotificationChanges = new ArrayList<>();

        for (Modifier lesson :
                notificationList) {
            calendar.setTime(lesson.getDate());
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (today == day)
                if (hour < 18)
                    todayNotificationChanges.add(lesson);
            else
                tomorrowNotificationChanges.add(lesson);
        }

        if (todayNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("היום:"));
            for (Modifier lesson :
                    todayNotificationChanges) {
                inboxStyle.addLine(lesson.getTitle());
            }
        }

        if (tomorrowNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("מחר:"));
            for (Modifier lesson :
                    tomorrowNotificationChanges) {
                inboxStyle.addLine(lesson.getTitle());
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
    private static Spanned getBoldText(String text) {
        return Html.fromHtml("<b> " + text + "</b>");
    }
}
