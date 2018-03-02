/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.blackcracks.blich.data.ClassGroup;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmSchema;

/**
 * A class containing utility methods to setup {@link Realm} and create queries.
 */
@SuppressWarnings("ConstantConditions")
public class RealmUtils {

    public static void setUpRealm(Context context) {
        io.realm.Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(11)
                .migration(new RealmMigration() {

                    @SuppressWarnings({"ConstantConditions", "UnusedAssignment"})
                    @Override
                    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {

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
                        if (oldVersion == 7) {
                            schema.create("Event")
                                    .addField("date", Date.class)
                                    .addField("name", String.class)
                                    .addField("beginHour", int.class)
                                    .addField("endHour", int.class)
                                    .addField("room", String.class);
                            schema.get("BlichData")
                                    .addRealmListField("events", schema.get("Event"));
                            oldVersion++;
                        }
                        if (oldVersion == 8) {
                            schema.get("Event")
                                    .addField("subject", String.class)
                                    .addField("teacher", String.class);
                            oldVersion++;
                        }
                        if (oldVersion == 9) {
                            schema.create("Exam")
                                    .addField("date", Date.class)
                                    .addField("name", String.class)
                                    .addField("beginHour", int.class)
                                    .addField("endHour", int.class)
                                    .addField("room", String.class)
                                    .addField("subject", String.class)
                                    .addField("teacher", String.class);
                            schema.get("BlichData")
                                    .addRealmListField("exams", schema.get("Exam"));
                            oldVersion++;
                        }
                        if (oldVersion == 10) {
                            schema.create("ClassGroup")
                                    .addField("id", int.class)
                                    .addField("name", String.class)
                                    .addField("grade", int.class)
                                    .addField("number", int.class);
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
     * Get a query object that contains all the filter rules.
     *
     * @param realm a {@link Realm} instance.
     * @param clazz type of class.
     * @param day day of the week.
     * @return a {@link RealmQuery}.
     */
    public static <E extends RealmModel> RealmQuery<E> buildFilteredQuery(
            Realm realm,
            Context context,
            Class<E> clazz,
            int day) {

        RealmQuery<E> query;
        switch (clazz.getSimpleName()) {
            case "Lesson":
                query = buildBaseLessonQuery(realm, clazz, day);
                break;
            default:
                query = buildBaseQuery(realm, clazz, day);
                break;
        }

        return buildFilteredQuery(query, context);
    }

    /**
     * Build a query on top of a query, containing all the filter rules.
     *
     * @param query a query to build upon.
     * @return a {@link RealmQuery}.
     */
    public static <E extends RealmModel> RealmQuery<E> buildFilteredQuery(
            RealmQuery<E> query,
            Context context) {


        String teacherFilter = PreferencesUtils.getString(
                context,
                Constants.Preferences.PREF_FILTER_SELECT_KEY);
        String[] teacherSubjects = teacherFilter.split(";");

        query.and()
                .beginGroup()
                .beginGroup()
                .equalTo("teacher", "")
                .and()
                .equalTo("subject", "")
                .endGroup();

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


    /**
     * Build a base query for lessons.
     *
     * @param realm a {@link Realm} instance.
     * @param clazz type of class.
     * @param day day of the week.
     * @return a {@link RealmQuery}.
     */
    private static <E extends RealmModel> RealmQuery<E> buildBaseLessonQuery(
            io.realm.Realm realm,
            Class<E> clazz,
            int day) {
        return realm.where(clazz)
                .equalTo("owners.day", day);
    }

    /**
     * Build a base query from day.
     *
     * @param realm a {@link Realm} instance.
     * @param clazz type of class.
     * @param day day of the week.
     * @return a {@link RealmQuery}.
     */
    public static <E extends RealmModel> RealmQuery<E> buildBaseQuery(
            Realm realm,
            Class<E> clazz,
            int day) {

        Date[] date = buildDatesBasedOnDay(day);
        return buildBaseQuery(realm, clazz, date[0], date[1]);
    }

    /**
     * Build a base query from dates.
     *
     * @param realm a {@link Realm} instance.
     * @param clazz type of class.
     * @param minDate the minimum date.
     * @param maxDate the maximum date.
     * @return a {@link RealmQuery}.
     */
    public static <E extends RealmModel> RealmQuery<E> buildBaseQuery(
            Realm realm,
            Class<E> clazz,
            Date minDate,
            Date maxDate) {

        return realm.where(clazz)
                .between("date", minDate, maxDate);
    }

    /**
     * @param day day of the week.
     * @return minimum and maximum dates for the day.
     */
    private static Date[] buildDatesBasedOnDay(int day) {
        Calendar calendar = Calendar.getInstance();
        //If Saturday, go to next week
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if ((today == 6 && hour > 18) || today == 7) calendar.add(Calendar.WEEK_OF_YEAR, 1);

        //Set time for today, 00:00 am
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date[] date = new Date[2];
        date[0] = calendar.getTime();

        //Set time for today 23:59 pm
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);

        date[1] = calendar.getTime();

        return date;
    }

    /**
     * Convert lessons to hour list.
     *
     * @param lessons a list of {@link Lesson}.
     * @param day day of the week.
     * @return list of {@link Hour}
     */
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

    /**
     * Convert lesson to hour list. Load the returned {@link Hour} list to RAM.
     *
     * @param lessons a list of {@link Lesson}.
     * @param day day of the week.
     * @return list of {@link Hour}
     */
    public static List<Hour> convertLessonListToHourRAM(Realm realm, List<Lesson> lessons, int day) {
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

            lesson = realm.copyFromRealm(lesson);
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

    /**
     * Get the id based on the grade and class number.
     *
     * @param realm a {@link Realm} instance.
     * @param gradeName a grade name.
     * @param classNum the grade/class index.
     * @return {@link ClassGroup} id.
     */
    public static int getId(Realm realm, String gradeName, int classNum) {
        int gradeNum = ClassGroup.gradeStringToNum(gradeName);
        ClassGroup classGroup = realm.where(ClassGroup.class)
                .equalTo("grade", gradeNum)
                .and()
                .equalTo("number", classNum)
                .findFirst();

        return classGroup.getId();
    }

    /**
     * Get the id based on the grade.
     *
     * @param realm a {@link Realm} instance.
     * @param name a grade name.
     * @return {@link ClassGroup} id.
     */
    public static int getId(Realm realm, String name) {
         return realm.where(ClassGroup.class)
                .equalTo("name", name)
                .findFirst()
                 .getId();

    }

    /**
     * @param realm a {@link Realm} instance.
     * @param id the id of the {@link ClassGroup}.
     * @return a {@link ClassGroup}.
     */
    public static ClassGroup getGrade(Realm realm, int id) {
        return realm.where(ClassGroup.class)
                .equalTo("id", id)
                .findFirst();
    }

}