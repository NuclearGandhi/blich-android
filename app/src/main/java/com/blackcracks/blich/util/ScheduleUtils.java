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
import com.blackcracks.blich.data.raw.Change;
import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawPeriod;
import com.blackcracks.blich.data.schedule.ModifiedLesson;
import com.blackcracks.blich.data.raw.Event;
import com.blackcracks.blich.data.raw.Exam;
import com.blackcracks.blich.data.schedule.ScheduleResult;

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
            int day,
            boolean loadToRAM) {
        //Check if the user wants to filter the schedule
        boolean isFilterOn = PreferenceUtils.getInstance().getBoolean(R.string.pref_filter_toggle_key);

        List<RawPeriod> RawPeriods;
        List<Change> changes;
        List<Event> events;
        List<Exam> exams;

        if (isFilterOn) { //Filter
            //Query using Inverse-Relationship and filter
            List<RawLesson> rawLessons = RealmUtils.buildFilteredQuery(
                    realm,
                    RawLesson.class,
                    day)
                    .findAll();

            if (loadToRAM) {
                RawPeriods = RealmUtils.convertLessonListToHourRAM(realm, rawLessons, day);
            } else {
                RawPeriods = RealmUtils.convertLessonListToHour(rawLessons, day);
            }
            Collections.sort(RawPeriods);

            changes = RealmUtils.buildFilteredQuery(
                    realm,
                    Change.class,
                    day)
                    .findAll();

            events = RealmUtils.buildFilteredQuery(
                    realm,
                    Event.class,
                    day)
                    .findAll();

            exams = RealmUtils.buildFilteredQuery(
                    realm,
                    Exam.class,
                    day)
                    .findAll();

        } else {//No filter, Query all
            RealmResults<RawPeriod> rawPeriodList = realm.where(RawPeriod.class)
                    .equalTo("day", day)
                    .findAll()
                    .sort("hour", Sort.ASCENDING);

            if (loadToRAM) {
                RawPeriods = realm.copyFromRealm(rawPeriodList);
            } else {
                RawPeriods = new ArrayList<>(rawPeriodList);
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

        List<ModifiedLesson> modifiedLessons = new ArrayList<ModifiedLesson>(changes);
        modifiedLessons.addAll(events);
        modifiedLessons.addAll(exams);

        return new ScheduleResult(RawPeriods, modifiedLessons);
    }

    public static void removeDuplicateDaterLessons(List<ModifiedLesson> list) {
        if (list.size() != 0 || list.size() != 1) {
            //Get all the changes, and remove all duplicate types
            //Build the comparator
            Comparator<ModifiedLesson> typeComparator = new Comparator<ModifiedLesson>() {
                @Override
                public int compare(ModifiedLesson o1, ModifiedLesson o2) {
                    return o1.getType().compareTo(o2.getType());
                }
            };

            //Sort
            Collections.sort(list, typeComparator);

            //Delete
            for (int i = 1; i < list.size(); i++) {
                ModifiedLesson lesson = list.get(i);
                ModifiedLesson prevLesson = list.get(i - 1);
                if (lesson.getType().equals(prevLesson.getType())) {
                    list.remove(lesson);
                    i--;
                }
            }
        }
    }

    public static void notifyUser(Context context) {
        List<ModifiedLesson> notificationList = fetchNotificationList();
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
     * @return a list of {@link ModifiedLesson}s.
     */
    private static List<ModifiedLesson> fetchNotificationList() {
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
                    Change.class);

            RealmUtils.buildFilteredQuery(
                    examsQuery,
                    Exam.class);

            RealmUtils.buildFilteredQuery(
                    eventsQuery,
                    Event.class);
        }

        List<ModifiedLesson> results = new ArrayList<>();
        results.addAll(changesQuery.findAll());
        results.addAll(examsQuery.findAll());
        results.addAll(eventsQuery.findAll());

        return results;
    }

    /**
     * Load the notification body with the changes.
     *
     * @param notificationList {@link ModifiedLesson}s to build content from.
     * @return notification body.
     */
    private static NotificationCompat.InboxStyle buildNotificationContent(List<ModifiedLesson> notificationList,
                                                                   NotificationCompat.Builder builder) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        List<ModifiedLesson> todayNotificationChanges = new ArrayList<>();
        List<ModifiedLesson> tomorrowNotificationChanges = new ArrayList<>();
        for (ModifiedLesson lesson :
                notificationList) {
            Calendar date = Calendar.getInstance();
            date.setTime(lesson.getDate());
            int day = date.get(Calendar.DAY_OF_WEEK);
            if (today == day) todayNotificationChanges.add(lesson);
            else tomorrowNotificationChanges.add(lesson);
        }

        if (todayNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("היום:"));
            for (ModifiedLesson lesson :
                    todayNotificationChanges) {
                inboxStyle.addLine(lesson.buildName());
            }
        }


        if (tomorrowNotificationChanges.size() != 0) {
            inboxStyle.addLine(getBoldText("מחר:"));
            for (ModifiedLesson lesson :
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
