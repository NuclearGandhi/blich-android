/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;

import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmSchema;
import timber.log.Timber;

public class RealmUtils {

    public static void setUpRealm(Context context) {
        io.realm.Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(7)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

                        RealmSchema schema = realm.getSchema();

                        if (oldVersion == 0) {
                            schema.get("Lesson")
                                    .addField("hour", int.class);
                            oldVersion++;
                        }
                        if (oldVersion == 1) {
                            schema.get("Lesson")
                                    .removeField("hour");
                            oldVersion++;
                        }
                        if (oldVersion == 2) {
                            schema.get("Schedule")
                                    .removeField("schedule")
                                    .addRealmListField("hours", schema.get("Hour"));
                            oldVersion++;
                        }
                        if (oldVersion == 3) {
                            schema.rename("Schedule", "BlichData");
                            oldVersion++;
                        }
                        if (oldVersion == 4) {
                            schema.create("Change")
                                    .addField("changeType", String.class)
                                    .addField("hour", int.class)
                                    .addField("subject", String.class)
                                    .addField("teacher", String.class)
                                    .addField("newTeacher", String.class)
                                    .addField("newRoom", String.class)
                                    .addField("newHour", int.class);
                            schema.get("BlichData")
                                    .addRealmListField("changes", schema.get("Change"));
                            oldVersion++;
                        }
                        if (oldVersion == 5) {
                            schema.get("Change")
                                    .addField("day", int.class);
                            oldVersion++;
                        }
                        if (oldVersion == 6) {
                            schema.get("Change")
                                    .removeField("day")
                                    .addField("date", Date.class);
                            oldVersion++;
                        }
                    }

                    @Override
                    public int hashCode() {
                        return 37;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof RealmMigration;
                    }
                })
                .build();
        io.realm.Realm.setDefaultConfiguration(config);
    }

    /**
     * Get a query object that contains all the filter rules
     *
     * @return {@link RealmQuery} object with filter rules
     */
    public static <E extends RealmObject> RealmQuery<E> buildFilteredQuery(
            Realm realm,
            Context context,
            Class<E> clazz,
            int day) {

        RealmQuery<E> query;
        switch (clazz.getSimpleName()) {
            case "Change": {
                query = buildBaseChangeQuery(realm, clazz, day);
                break;
            }
            default: {
                query = buildBaseLessonQuery(realm, clazz, day);
                break;
            }
        }

        return buildFilteredQuery(query, context);
    }

    public static <E extends RealmObject> RealmQuery<E> buildFilteredQuery(
            RealmQuery<E> query,
            Context context) {


        String teacherFilter = PreferencesUtils.getString(
                context,
                Constants.Preferences.PREF_FILTER_SELECT_KEY);
        String[] teacherSubjects = teacherFilter.split(";");

        query.and()
                .beginGroup()
                //Set an impossible case for easier code writing
                .equalTo("subject", "oghegijd39");

        for (String teacherSubject :
                teacherSubjects) {
            if (teacherSubject.equals("")) break;

            String[] arr = teacherSubject.split(",");
            String teacher = arr[0];
            String subject = arr[1];

            query.or()
                    .beginGroup()
                    .equalTo("teacher", teacher)
                    .and()
                    .equalTo("subject", subject)
                    .endGroup();
        }

        query.endGroup();

        return query;
    }


    public static <E extends RealmObject> RealmQuery<E> buildBaseLessonQuery(
            io.realm.Realm realm,
            Class<E> clazz,
            int day) {
        return realm.where(clazz)
                .equalTo("day", day);
    }

    public static <E extends RealmObject> RealmQuery<E> buildBaseChangeQuery(
            Realm realm,
            Class<E> clazz,
            int day) {

        Calendar calendar = Calendar.getInstance();

        //Set time for today, 00:00 am
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date minDate = calendar.getTime();

        //Set time for today 23:59 pm
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);

        Date maxDate = calendar.getTime();

        return buildBaseChangeQuery(realm, clazz, minDate, maxDate);
    }

    public static <E extends RealmObject> RealmQuery<E> buildBaseChangeQuery(
            Realm realm,
            Class<E> clazz,
            Date minDate,
            Date maxDate) {

        RealmQuery<E> query = realm.where(clazz)
                .between("date", minDate, maxDate);
        return query;
    }

    public static List<Hour> convertLessonListToHour(List<Lesson> lessons, int day) {
        //Translate the lesson list to hour list
        List<Hour> results = new ArrayList<>();
        for (Lesson lesson :
                lessons) {
            int hourNum = lesson.getOwners().get(0).getHour();
            Hour hour = null;

            for (Hour result :
                    results) {
                if (result.getHour() == hourNum) hour = result;
            }

            if (hour == null) {
                RealmList<Lesson> lessonList = new RealmList<>();
                lessonList.add(lesson);
                hour = new Hour(day, hourNum, lessonList);
                results.add(hour);
            } else {
                hour.getLessons().add(lesson);
            }
        }

        return results;
    }

    public static List<Hour> convertLessonListToHour(List<Lesson> lessons, int day, SparseIntArray hourArr) {
        //Translate the lesson list to hour list
        List<Hour> results = new ArrayList<>();
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            int hourNum = hourArr.get(i);
            Hour hour = null;

            for (Hour result :
                    results) {
                if (result.getHour() == hourNum) hour = result;
            }

            if (hour == null) {
                RealmList<Lesson> lessonList = new RealmList<>();
                lessonList.add(lesson);
                hour = new Hour(day, hourNum, lessonList);
                results.add(hour);
            } else {
                hour.getLessons().add(lesson);
            }
        }

        return results;
    }

    public static class RealmScheduleHelper {
        private List<Hour> mHours;
        private List<Change> mChanges;
        private boolean mIsDataValid;

        public RealmScheduleHelper(ScheduleResult data) {
            switchData(data);
        }

        public void switchData(ScheduleResult data) {
            if (data != null) {
                mHours = data.getHours();
                mChanges = data.getChanges();
            }

            try {
                mIsDataValid = data != null && mHours != null && mChanges != null && !mHours.isEmpty();
            } catch (IllegalStateException e) { //In case Realm instance has been closed
                mIsDataValid = false;
                Timber.d("Realm has been closed");
            }
        }

        public boolean isDataValid() {
            return mIsDataValid;
        }

        public Hour getHour(int position) {
            return mHours.get(position);
        }

        public List<Change> getChanges(int hour) {
            List<Change> changes = new ArrayList<>();
            for (Change change :
                    mChanges) {
                if (change.getHour() == hour) changes.add(change);
            }

            return changes;
        }

        public Lesson getLesson(int position, int childPos) {
            if (!mIsDataValid) return null;
            Hour hour = getHour(position);
            return hour.getLessons().get(childPos);
        }

        public @Nullable
        Change getChange(int hour, Lesson lesson) {
            List<Change> hourChanges = getChanges(hour);
            for (int i = 0; i < hourChanges.size(); i++) {
                Change change = hourChanges.get(i);
                if (change.getTeacher().equals(lesson.getTeacher()) &&
                        change.getSubject().equals(lesson.getSubject()))
                    return change;
            }
            return null;
        }

        public int getHourCount() {
            if (mIsDataValid) {
                return mHours.size();
            } else {
                return 0;
            }
        }

        public int getChildCount(int position) {
            if (mIsDataValid) {
                return getHour(position).getLessons().size();
            } else {
                return 0;
            }
        }
    }
}
