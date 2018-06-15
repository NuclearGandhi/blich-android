/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.sync;

import android.content.Context;
import android.os.Bundle;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.TeacherSubject;
import com.blackcracks.blich.data.exam.Exam;
import com.blackcracks.blich.data.raw.Change;
import com.blackcracks.blich.data.raw.RawEvent;
import com.blackcracks.blich.data.raw.RawExam;
import com.blackcracks.blich.data.raw.RawData;
import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawPeriod;
import com.blackcracks.blich.data.schedule.Lesson;
import com.blackcracks.blich.data.schedule.Modifier;
import com.blackcracks.blich.data.schedule.Period;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.ShahafUtils;
import com.blackcracks.blich.util.SyncCallbackUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * A class to handle the sync, meaning getting all the necessary data for the database.
 */
public class BlichSyncTask {

    private static final String EVENT_BEGIN_SYNC = "begin_sync";
    private static final String EVENT_END_SYNC = "end_sync";

    private static final String LOG_PARAM_STATUS_SYNC = "status";

    /**
     * Begin syncing the data.
     *
     * @return a {@link SyncCallbackUtils.FetchStatus} returned by the sync.
     */
    public static @SyncCallbackUtils.FetchStatus
    int syncBlich(Context context) {
        //Log the beginning of sync
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(EVENT_BEGIN_SYNC, Bundle.EMPTY);

        RawData rawData = new RawData();
        int status = fetchData(context, rawData);

        if (status == SyncCallbackUtils.FETCH_STATUS_SUCCESSFUL) {//Don't load data if fetch failed
            insertDataIntoRealm(
                    ShahafUtils.processScheduleRawData(rawData),
                    ShahafUtils.processExamRawData(rawData),
                    ShahafUtils.processTeacherSubjectData(rawData)
            );
        }

        //Log the end of sync
        PreferenceUtils.getInstance().putBoolean(R.string.pref_is_syncing_key, false);

        Bundle bundle = new Bundle();
        bundle.putInt(LOG_PARAM_STATUS_SYNC, status);
        firebaseAnalytics.logEvent(EVENT_END_SYNC, bundle);
        return status;
    }

    /**
     * Fetch the required data from the server.
     *
     * @param rawData Data object to insert the fetched data into.
     * @return a {@link SyncCallbackUtils.FetchStatus}.
     */
    private static @SyncCallbackUtils.FetchStatus
    int fetchData(Context context, RawData rawData) {
        //Call four different requests from the server.
        for (int i = 0; i < 4; i++) {
            String json;

            String command = ShahafUtils.COMMAND_SCHEDULE;
            switch (i) {
                case 0: {
                    command = ShahafUtils.COMMAND_SCHEDULE;
                    break;
                }
                case 1: {
                    command = ShahafUtils.COMMAND_CHANGES;
                    break;
                }
                case 2: {
                    command = ShahafUtils.COMMAND_EVENTS;
                    break;
                }
                case 3: {
                    command = ShahafUtils.COMMAND_EXAMS;
                    break;
                }
            }

            try {
                json = ShahafUtils.getResponseFromUrl(ShahafUtils.buildUrlFromCommand(context, command));

                if (json == null || json.equals(""))
                    return SyncCallbackUtils.FETCH_STATUS_UNSUCCESSFUL;

                //Insert data accordingly
                switch (command) {
                    case ShahafUtils.COMMAND_SCHEDULE: {
                        insertScheduleJsonIntoData(json, rawData);
                        break;
                    }
                    case ShahafUtils.COMMAND_CHANGES: {
                        insertChangesJsonIntoData(json, rawData);
                        break;
                    }
                    case ShahafUtils.COMMAND_EVENTS: {
                        insertEventsJsonIntoData(json, rawData);
                        break;
                    }
                    case ShahafUtils.COMMAND_EXAMS: {
                        insertExamsJsonIntoData(json, rawData);
                        break;
                    }
                }
            } catch (IOException | JSONException e) {
                Timber.e(e);
                return SyncCallbackUtils.FETCH_STATUS_UNSUCCESSFUL;
            }
        }

        return SyncCallbackUtils.FETCH_STATUS_SUCCESSFUL;
    }

    private static void insertScheduleJsonIntoData(String json, RawData rawData) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray jsonHours = raw.getJSONArray(Database.JSON_ARRAY_HOURS);
        RealmList<RawPeriod> rawPeriods = new RealmList<>();
        for (int i = 0; i < jsonHours.length(); i++) {
            RawPeriod RawPeriod = new RawPeriod();
            JSONObject jsonHour = jsonHours.getJSONObject(i);

            RealmList<RawLesson> rawLessons = new RealmList<>();
            JSONArray jsonLessons = jsonHour.getJSONArray(Database.JSON_ARRAY_LESSONS);
            for (int j = 0; j < jsonLessons.length(); j++) {
                RawLesson rawLesson = new RawLesson();
                JSONObject jsonLesson = jsonLessons.getJSONObject(j);

                rawLesson.setSubject(jsonLesson.getString(Database.JSON_STRING_SUBJECT));
                rawLesson.setTeacher(jsonLesson.getString(Database.JSON_STRING_TEACHER));
                rawLesson.setRoom(jsonLesson.getString(Database.JSON_STRING_ROOM));

                rawLessons.add(rawLesson);
            }

            int day = (jsonHour.getInt(Database.JSON_INT_DAY) + 1) % 7;

            RawPeriod.setLessons(rawLessons);
            RawPeriod.setHour(jsonHour.getInt(Database.JSON_INT_HOUR));
            RawPeriod.setDay(day);

            rawPeriods.add(RawPeriod);
        }

        rawData.setRawPeriods(rawPeriods);
    }

    private static void insertChangesJsonIntoData(String json, RawData rawData) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray jsonChanges = raw.getJSONArray(Database.JSON_ARRAY_CHANGES);
        RealmList<Change> changes = new RealmList<>();
        for (int i = 0; i < jsonChanges.length(); i++) {
            Change change = new Change();
            JSONObject jsonChange = jsonChanges.getJSONObject(i);
            JSONObject jsonStudyGroup = jsonChange.getJSONObject(Database.JSON_OBJECT_STUDY_GROUP);

            String jsonDate = jsonChange.getString(Database.JSON_STRING_DATE);
            Date date = parseDate(jsonDate);

            change.setChangeType(jsonChange.getString(Database.JSON_STRING_CHANGE_TYPE));
            change.setDate(date);
            change.setBeginHour(jsonChange.getInt(Database.JSON_INT_HOUR));
            change.setEndHour(jsonChange.getInt(Database.JSON_INT_HOUR));
            change.setSubject(jsonStudyGroup.getString(Database.JSON_STRING_SUBJECT));
            change.setOldTeacher(jsonStudyGroup.getString(Database.JSON_STRING_TEACHER));
            change.setNewHour(jsonChange.getInt(Database.JSON_INT_NEW_HOUR));
            change.setNewTeacher(jsonChange.getString(Database.JSON_STRING_NEW_TEACHER));
            change.setNewRoom(jsonChange.getString(Database.JSON_STRING_NEW_ROOM));

            if (change.getChangeType().equals(Database.TYPE_NEW_HOUR)) {
                changes.add(
                        change.cloneNewHourVariant()
                );
            }
            changes.add(change);
        }

        rawData.addModifiedLessons(changes);
    }

    private static void insertEventsJsonIntoData(String json, RawData rawData) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray jsonEvents = raw.getJSONArray(Database.JSON_ARRAY_EVENTS);
        RealmList<RawEvent> rawEvents = new RealmList<>();
        for (int i = 0; i < jsonEvents.length(); i++) {
            JSONObject jsonEvent = jsonEvents.getJSONObject(i);
            RawEvent rawEvent = new RawEvent();

            Date date = parseDate(jsonEvent.getString(Database.JSON_STRING_DATE));

            String subject = null;
            String teacher = null;
            if (!jsonEvent.isNull(Database.JSON_OBJECT_STUDY_GROUP)) {
                JSONObject studyGroup = jsonEvent.getJSONObject(Database.JSON_OBJECT_STUDY_GROUP);
                subject = studyGroup.getString(Database.JSON_STRING_SUBJECT);
                teacher = studyGroup.getString(Database.JSON_STRING_TEACHER);
            }


            rawEvent.setDate(date);
            rawEvent.setTitle(jsonEvent.getString(Database.JSON_NAME));
            rawEvent.setBeginHour(jsonEvent.getInt(Database.JSON_INT_BEGIN_HOUR));
            rawEvent.setEndHour(jsonEvent.getInt(Database.JSON_INT_END_HOUR));
            rawEvent.setOldRoom(jsonEvent.getString(Database.JSON_STRING_ROOM));
            rawEvent.setSubject(subject);
            rawEvent.setOldTeacher(teacher);

            rawEvents.add(rawEvent);
        }

        rawData.addModifiedLessons(rawEvents);
    }

    private static void insertExamsJsonIntoData(String json, RawData rawData) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray jsonExams = raw.getJSONArray(Database.JSON_ARRAY_EXAMS);
        RealmList<RawExam> rawExams = new RealmList<>();
        for (int i = 0; i < jsonExams.length(); i++) {
            JSONObject jsonExam = jsonExams.getJSONObject(i);
            RawExam rawExam = new RawExam();

            Date date = parseDate(jsonExam.getString(Database.JSON_STRING_DATE));
            String subject = null;
            String teacher = null;
            if (!jsonExam.isNull(Database.JSON_OBJECT_STUDY_GROUP)) {
                JSONObject studyGroup = jsonExam.getJSONObject(Database.JSON_OBJECT_STUDY_GROUP);
                subject = studyGroup.getString(Database.JSON_STRING_SUBJECT);
                teacher = studyGroup.getString(Database.JSON_STRING_TEACHER);
            }

            rawExam.setDate(date);
            rawExam.setTitle(jsonExam.getString(Database.JSON_NAME));
            rawExam.setBeginHour(jsonExam.getInt(Database.JSON_INT_BEGIN_HOUR));
            rawExam.setEndHour(jsonExam.getInt(Database.JSON_INT_END_HOUR));
            rawExam.setOldRoom(jsonExam.getString(Database.JSON_STRING_ROOM));
            rawExam.setSubject(subject);
            rawExam.setOldTeacher(teacher);

            rawExams.add(rawExam);
        }

        rawData.addModifiedLessons(rawExams);
    }

    private static void insertDataIntoRealm(
            RealmList<Period> schedule,
            RealmList<Exam> exams,
            RealmList<TeacherSubject> teacherSubjects) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults<Period> oldSchedule = realm.where(Period.class)
                .findAll();
        oldSchedule.deleteAllFromRealm();

        RealmResults<Modifier> oldModifiers = realm.where(Modifier.class)
                .findAll();
        oldModifiers.deleteAllFromRealm();

        RealmResults<Lesson> oldLessons = realm.where(Lesson.class)
                .findAll();
        oldLessons.deleteAllFromRealm();

        RealmResults<Exam> oldExams = realm.where(Exam.class)
                .findAll();
        oldExams.deleteAllFromRealm();

        RealmResults<TeacherSubject> oldTeacherSubjects = realm.where(TeacherSubject.class)
                .findAll();
        oldTeacherSubjects.deleteAllFromRealm();

        realm.insert(schedule);
        realm.insert(exams);
        realm.insert(teacherSubjects);
        realm.commitTransaction();

        realm.close();
    }

    /**
     * The date given in the json is required to be filtered before insertion to database.
     *
     * @param jsonDate unfiltered date in the form of a {@link String}.
     * @return a {@link Date}.
     */
    private static Date parseDate(String jsonDate) {
        int firstCut = jsonDate.indexOf("(") + 1;
        int lastCut = jsonDate.indexOf(")");
        long timeInMillis = Long.parseLong(jsonDate.substring(firstCut, lastCut));
        return new Date(timeInMillis);
    }
}
