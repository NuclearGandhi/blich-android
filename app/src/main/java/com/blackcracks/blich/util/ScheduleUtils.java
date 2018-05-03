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
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.receiver.ScheduleAlarmReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

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

        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 18 && day != 7)
            day++; //Move to the next day if it is later than 18:00, unless it is Saturday.
        if (day == 7) day = 1; //If it is Saturday, set day to 1 (Sunday).
        return day;
    }

    /**
     * Fetch from {@link Realm} schedule and pack it into a {@link ScheduleResult}.
     *
     * @param realm     a {@link Realm} instance.
     * @param day       day of the week.
     * @param loadToRAM {@code true} copy the data from realm.
     * @return the {@link ScheduleResult}.
     */
    public static ScheduleResult fetchScheduleResult(
            Realm realm,
            Context context,
            int day,
            boolean loadToRAM) {
        //Check if the user wants to filter the schedule
        boolean isFilterOn = PreferenceUtils.getInstance().getBoolean(R.string.pref_filter_toggle_key);

        List<Hour> hours;
        List<Change> changes;
        List<Event> events;
        List<Exam> exams;

        if (isFilterOn) { //Filter
            //Query using Inverse-Relationship and filter
            List<Lesson> lessons = RealmUtils.buildFilteredQuery(
                    realm,
                    context,
                    Lesson.class,
                    day)
                    .findAll();

            if (loadToRAM) {
                hours = RealmUtils.convertLessonListToHourRAM(realm, lessons, day);
            } else {
                hours = RealmUtils.convertLessonListToHour(lessons, day);
            }
            Collections.sort(hours);

            changes = RealmUtils.buildFilteredQuery(
                    realm,
                    context,
                    Change.class,
                    day)
                    .findAll();

            events = RealmUtils.buildFilteredQuery(
                    realm,
                    context,
                    Event.class,
                    day)
                    .findAll();

            exams = RealmUtils.buildFilteredQuery(
                    realm,
                    context,
                    Exam.class,
                    day)
                    .findAll();

        } else {//No filter, Query all
            RealmResults<Hour> hourList = realm.where(Hour.class)
                    .equalTo("day", day)
                    .findAll()
                    .sort("hour", Sort.ASCENDING);

            if (loadToRAM) {
                hours = realm.copyFromRealm(hourList);
            } else {
                hours = new ArrayList<>(hourList);
            }

            changes = RealmUtils.buildBaseQuery(
                    realm,
                    Change.class,
                    day)
                    .findAll();

            events = RealmUtils.buildBaseQuery(
                    realm,
                    Event.class,
                    day)
                    .findAll();

            exams = RealmUtils.buildBaseQuery(
                    realm,
                    Exam.class,
                    day)
                    .findAll();
        }

        if (loadToRAM) {
            changes = realm.copyFromRealm(changes);
            events = realm.copyFromRealm(events);
            exams = realm.copyFromRealm(exams);
        }

        List<DatedLesson> datedLessons = new ArrayList<DatedLesson>(changes);
        datedLessons.addAll(events);
        datedLessons.addAll(exams);

        return new ScheduleResult(hours, datedLessons);
    }

    public static void removeDuplicateDaterLessons(List<DatedLesson> list) {
        if (list.size() != 0 || list.size() != 1) {
            //Get all the changes, and remove all duplicate types
            //Build the comparator
            Comparator<DatedLesson> typeComparator = new Comparator<DatedLesson>() {
                @Override
                public int compare(DatedLesson o1, DatedLesson o2) {
                    return o1.getType().compareTo(o2.getType());
                }
            };

            //Sort
            Collections.sort(list, typeComparator);

            //Delete
            for (int i = 1; i < list.size(); i++) {
                DatedLesson lesson = list.get(i);
                DatedLesson prevLesson = list.get(i - 1);
                if (lesson.getType().equals(prevLesson.getType())) {
                    list.remove(lesson);
                    i--;
                }
            }
        }
    }

    public static void notifyUser(Context context) {
        List<DatedLesson> notificationList = fetchNotificationList(context);
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
    }

    /**
     * Fetch all the changes in the schedule.
     *
     * @return a list of {@link DatedLesson}s.
     */
    private static List<DatedLesson> fetchNotificationList(Context context) {
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
                    context,
                    Change.class);

            RealmUtils.buildFilteredQuery(
                    examsQuery,
                    context,
                    Exam.class);

            RealmUtils.buildFilteredQuery(
                    eventsQuery,
                    context,
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
    private static NotificationCompat.InboxStyle buildNotificationContent(List<DatedLesson> notificationList,
                                                                   NotificationCompat.Builder builder) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        List<DatedLesson> todayNotificationChanges = new ArrayList<>();
        List<DatedLesson> tomorrowNotificationChanges = new ArrayList<>();
        for (DatedLesson lesson :
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
    private static Spanned getBoldText(String text) {
        return Html.fromHtml("<b> " + text + "</b>");
    }
}
