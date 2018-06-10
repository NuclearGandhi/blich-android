/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.raw.ClassGroup;
import com.blackcracks.blich.data.schedule.Lesson;
import com.blackcracks.blich.data.schedule.Period;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;

/**
 * A class containing utility methods to setup {@link Realm} and create queries.
 */
@SuppressWarnings("ConstantConditions")
public class RealmUtils {

    public static void setUpRealm(Context context) {
        io.realm.Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(BlichMigration.DATABASE_VERSION)
                .migration(new BlichMigration())
                .build();
        io.realm.Realm.setDefaultConfiguration(config);
    }

    public static List<Period> convertLessonListToPeriodList(List<Lesson> lessons, int day) {
        List<Period> results = new ArrayList<>();
        for (Lesson lesson :
                lessons) {
            int periodNum = lesson.getOwners().size() != 0 ?
                    lesson.getOwners().get(0).getPeriodNum() :
                    lesson.getOtherOwners().get(0).getPeriodNum();
            Period period = null;

            for (Period result :
                    results) {
                if (result.getPeriodNum() == periodNum) period = result;
            }

            if (period == null) {
                RealmList<Lesson> lessonsList = new RealmList<>();
                lessonsList.add(lesson);
                period = new Period(day, lessonsList, periodNum);
                results.add(period);

                if (lesson.getModifier() != null) {
                    period.addChangeTypeColor(lesson.getModifier().getColor());
                }
            } else {
                period.getItems().add(lesson);
            }
        }

        for (Period period : results) {
            period.setFirstLesson(period.getItems().get(0));
            period.getItems().remove(0);
        }

        Collections.sort(results);

        return results;
    }

    public static List<Period> convertLessonListToPeriodList(Realm realm, RealmResults<Lesson> lessons, int day) {
        List<Period> results = new ArrayList<>();
        for (Lesson lesson :
                lessons) {
            int periodNum = lesson.getOwners().size() != 0 ?
                    lesson.getOwners().get(0).getPeriodNum() :
                    lesson.getOtherOwners().get(0).getPeriodNum();
            Period period = null;

            lesson = realm.copyFromRealm(lesson);
            for (Period result :
                    results) {
                if (result.getPeriodNum() == periodNum) period = result;
            }

            if (period == null) {
                RealmList<Lesson> lessonsList = new RealmList<>();
                lessonsList.add(lesson);
                period = new Period(day, lessonsList, periodNum);
                results.add(period);

                if (lesson.getModifier() != null) {
                    period.addChangeTypeColor(lesson.getModifier().getColor());
                }
            } else {
                period.getItems().add(lesson);
            }
        }

        for (Period period : results) {
            period.setFirstLesson(period.getItems().get(0));
            period.getItems().remove(0);
        }

        Collections.sort(results);

        return results;
    }

    /**
     * Get the id based on the grade and class number.
     *
     * @param realm     a {@link Realm} instance.
     * @param gradeName a grade name.
     * @param classNum  the grade/class index.
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
     * @param name  a grade name.
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
     * @param id    the id of the {@link ClassGroup}.
     * @return a {@link ClassGroup}.
     */
    public static @Nullable
    ClassGroup getGrade(Realm realm, int id) {
        return realm.where(ClassGroup.class)
                .equalTo("id", id)
                .findFirst();
    }

    private static class BlichMigration implements RealmMigration {

        private static final int DATABASE_VERSION = 14;

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
                        .addRealmListField("hours", schema.get("Period"));
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
            if (oldVersion == 11) {
                schema.get("Lesson")
                        .removeField("changeType");
                oldVersion++;
            }
            if (oldVersion == 12) {
                schema.rename("Lesson", "RawLesson");
                schema.get("RawLesson")
                        .removeField("changeType");
                schema.rename("Hour", "RawPeriod");
                schema.get("BlichData")
                        .renameField("hours", "periods");
                oldVersion++;
            }
            if (oldVersion == 13) {
                schema.remove("RawLesson");
                schema.remove("RawPeriod");
                schema.remove("Change");
                schema.remove("Event");
                schema.remove("Exam");
                schema.remove("BlichData");

                schema.create("Modifier")
                        .addField("modifierType", int.class)
                        .addField("title", String.class)
                        .addField("subject", String.class)
                        .addField("oldTeacher", String.class)
                        .addField("newTeacher", String.class)
                        .addField("isAReplacer", boolean.class)
                        .addField("date", Date.class)
                        .addField("beginPeriod", int.class)
                        .addField("endPeriod", int.class);

                schema.create("Lesson")
                        .addField("subject", String.class)
                        .addField("teacher", String.class)
                        .addField("room", String.class)
                        .addRealmObjectField("modifier", schema.get("Modifier"));

                schema.create("Period")
                        .addField("day", int.class)
                        .addField("periodNum", int.class)
                        .addRealmListField("lessons", schema.get("Lesson"))
                        .addRealmObjectField("firstLesson", schema.get("Lesson"))
                        .addRealmListField("changeTypeColors", int.class);

                schema.create("Exam")
                        .addField("date", Date.class)
                        .addField("title", String.class)
                        .addField("baseTitle", String.class)
                        .addField("beginPeriod", int.class)
                        .addField("endPeriod", int.class)
                        .addRealmListField("teachers", String.class);

                schema.create("TeacherSubject")
                        .addField("teacher", String.class)
                        .addField("subject", String.class);
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
    }
}