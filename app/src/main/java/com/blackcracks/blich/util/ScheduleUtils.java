/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;

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
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ScheduleUtils {

    public static int getWantedDayOfTheWeek() {

        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 18 && day != 7)
            day++; //Move to the next day if it is later than 18:00, unless it is Saturday.
        if (day == 7) day = 1; //If it is Saturday, set day to 1 (Sunday).
        return day;
    }

    public static ScheduleResult fetchScheduleResult(Realm realm, Context context, int day) {
        //Check if the user wants to filter the schedule
        boolean isFilterOn = PreferencesUtils.getBoolean(
                context,
                Constants.Preferences.PREF_FILTER_TOGGLE_KEY);

        List<Hour> hours;
        List<Change> changes;
        List<Event> events;
        List<Exam> exams;

        if (isFilterOn) { //Filter
            //Query using Inverse-Relationship and filter
            RealmResults<Lesson> lessons = RealmUtils.buildFilteredQuery(
                    realm,
                    context,
                    Lesson.class,
                    day)
                    .findAll();

            hours = RealmUtils.convertLessonListToHour(lessons, day);
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

            hours = new ArrayList<>(hourList);

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

        List<DatedLesson> datedLessons = new ArrayList<DatedLesson>(changes);
        datedLessons.addAll(events);
        datedLessons.addAll(exams);

        return new ScheduleResult(hours, datedLessons);
    }
}
