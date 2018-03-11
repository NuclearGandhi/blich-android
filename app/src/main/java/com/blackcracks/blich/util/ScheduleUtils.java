/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import android.content.Context;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A class containing utility methods for schedule.
 */
public class ScheduleUtils {

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
     * @param realm a {@link Realm} instance.
     * @param day day of the week.
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
}
