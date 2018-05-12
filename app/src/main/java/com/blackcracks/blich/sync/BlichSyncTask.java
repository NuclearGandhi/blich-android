/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.sync;

import android.content.Context;
import android.os.Bundle;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichData;
import com.blackcracks.blich.data.raw.Change;
import com.blackcracks.blich.data.raw.Event;
import com.blackcracks.blich.data.raw.Exam;
import com.blackcracks.blich.data.raw.Hour;
import com.blackcracks.blich.data.raw.Lesson;
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

        BlichData blichData = new BlichData();
        int status = fetchData(blichData);

        if (status == SyncCallbackUtils.FETCH_STATUS_SUCCESSFUL) {//Don't load data if fetch failed
            loadDataIntoRealm(blichData);
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
     * @param blichData Data object to insert the fetched data into.
     * @return a {@link SyncCallbackUtils.FetchStatus}.
     */
    private static @SyncCallbackUtils.FetchStatus
    int fetchData(BlichData blichData) {
        //Call four different requests from the server.
        for(int i = 0; i < 4; i++) {
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
                json = ShahafUtils.getResponseFromUrl(ShahafUtils.buildUrlFromCommand(command));

                if (json == null || json.equals("")) return SyncCallbackUtils.FETCH_STATUS_UNSUCCESSFUL;

                //Insert data accordingly
                switch (command) {
                    case ShahafUtils.COMMAND_SCHEDULE: {
                        insertScheduleJsonIntoData(json, blichData);
                        break;
                    }
                    case ShahafUtils.COMMAND_CHANGES: {
                        insertChangesJsonIntoData(json, blichData);
                        break;
                    }
                    case ShahafUtils.COMMAND_EVENTS: {
                        insertEventsJsonIntoData(json, blichData);
                        break;
                    }
                    case ShahafUtils.COMMAND_EXAMS: {
                        insertExamsJsonIntoData(json, blichData);
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

    private static void insertScheduleJsonIntoData(String json, BlichData blichData) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray jsonHours = raw.getJSONArray(Database.JSON_ARRAY_HOURS);
        RealmList<Hour> hours = new RealmList<>();
        for (int i = 0; i < jsonHours.length(); i++) {
            Hour hour = new Hour();
            JSONObject jsonHour = jsonHours.getJSONObject(i);

            RealmList<Lesson> lessons = new RealmList<>();
            JSONArray jsonLessons = jsonHour.getJSONArray(Database.JSON_ARRAY_LESSONS);
            for (int j = 0; j < jsonLessons.length(); j++) {
                Lesson lesson = new Lesson();
                JSONObject jsonLesson = jsonLessons.getJSONObject(j);

                lesson.setSubject(jsonLesson.getString(Database.JSON_STRING_SUBJECT));
                lesson.setTeacher(jsonLesson.getString(Database.JSON_STRING_TEACHER));
                lesson.setRoom(jsonLesson.getString(Database.JSON_STRING_ROOM));

                lessons.add(lesson);
            }

            int day = (jsonHour.getInt(Database.JSON_INT_DAY) + 1) % 7;

            hour.setLessons(lessons);
            hour.setHour(jsonHour.getInt(Database.JSON_INT_HOUR));
            hour.setDay(day);

            hours.add(hour);
        }

        blichData.setHours(hours);
        blichData.setClassId(raw.getInt(Database.JSON_INT_CLASS_ID));
    }

    private static void insertChangesJsonIntoData(String json, BlichData blichData) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray jsonChanges = raw.getJSONArray(Database.JSON_ARRAY_CHANGES);
        RealmList<Change> changes = new RealmList<>();
        for(int i = 0; i < jsonChanges.length(); i++) {
            Change change = new Change();
            JSONObject jsonChange = jsonChanges.getJSONObject(i);
            JSONObject jsonStudyGroup = jsonChange.getJSONObject(Database.JSON_OBJECT_STUDY_GROUP);

            String jsonDate = jsonChange.getString(Database.JSON_STRING_DATE);
            Date date = parseDate(jsonDate);

            change.setChangeType(jsonChange.getString(Database.JSON_STRING_CHANGE_TYPE));
            change.setDate(date);
            change.setHour(jsonChange.getInt(Database.JSON_INT_HOUR));
            change.setSubject(jsonStudyGroup.getString(Database.JSON_STRING_SUBJECT));
            change.setTeacher(jsonStudyGroup.getString(Database.JSON_STRING_TEACHER));
            change.setNewHour(jsonChange.getInt(Database.JSON_INT_NEW_HOUR));
            change.setNewTeacher(jsonChange.getString(Database.JSON_STRING_NEW_TEACHER));
            change.setNewRoom(jsonChange.getString(Database.JSON_STRING_NEW_ROOM));

            if (change.getChangeType().equals(Database.TYPE_NEW_HOUR)) {
                try {
                    Change otherChange = (Change) change.clone();
                    otherChange.setHour(change.getNewHour());
                    changes.add(otherChange);
                } catch (CloneNotSupportedException e) {
                    Timber.e(e);
                }
            }
            changes.add(change);
        }

        blichData.setChanges(changes);
    }

    private static void insertEventsJsonIntoData(String json, BlichData blichData) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray jsonEvents = raw.getJSONArray(Database.JSON_ARRAY_EVENTS);
        RealmList<Event> events = new RealmList<>();
        for(int i = 0; i < jsonEvents.length(); i++) {
            JSONObject jsonEvent = jsonEvents.getJSONObject(i);
            Event event = new Event();

            Date date = parseDate(jsonEvent.getString(Database.JSON_STRING_DATE));

            String subject = "";
            String teacher = "";
            if (!jsonEvent.isNull(Database.JSON_OBJECT_STUDY_GROUP)) {
                JSONObject studyGroup = jsonEvent.getJSONObject(Database.JSON_OBJECT_STUDY_GROUP);
                subject = studyGroup.getString(Database.JSON_STRING_SUBJECT);
                teacher = studyGroup.getString(Database.JSON_STRING_TEACHER);
            }


            event.setDate(date);
            event.setName(jsonEvent.getString(Database.JSON_NAME));
            event.setBeginHour(jsonEvent.getInt(Database.JSON_INT_BEGIN_HOUR));
            event.setEndHour(jsonEvent.getInt(Database.JSON_INT_END_HOUR));
            event.setRoom(jsonEvent.getString(Database.JSON_STRING_ROOM));
            event.setSubject(subject);
            event.setTeacher(teacher);

            events.add(event);
        }

        blichData.setEvents(events);
    }

    private static void insertExamsJsonIntoData(String json, BlichData blichData) throws JSONException{
        JSONObject raw = new JSONObject(json);

        JSONArray jsonExams = raw.getJSONArray(Database.JSON_ARRAY_EXAMS);
        RealmList<Exam> exams = new RealmList<>();
        for (int i = 0; i < jsonExams.length(); i++) {
            JSONObject jsonExam = jsonExams.getJSONObject(i);
            Exam exam = new Exam();

            Date date = parseDate(jsonExam.getString(Database.JSON_STRING_DATE));
            String subject = "";
            String teacher = "";
            if (!jsonExam.isNull(Database.JSON_OBJECT_STUDY_GROUP)) {
                JSONObject studyGroup = jsonExam.getJSONObject(Database.JSON_OBJECT_STUDY_GROUP);
                subject = studyGroup.getString(Database.JSON_STRING_SUBJECT);
                teacher = studyGroup.getString(Database.JSON_STRING_TEACHER);
            }


            exam.setDate(date);
            exam.setName(jsonExam.getString(Database.JSON_NAME));
            exam.setBeginHour(jsonExam.getInt(Database.JSON_INT_BEGIN_HOUR));
            exam.setEndHour(jsonExam.getInt(Database.JSON_INT_END_HOUR));
            exam.setRoom(jsonExam.getString(Database.JSON_STRING_ROOM));
            exam.setSubject(subject);
            exam.setTeacher(teacher);

            exams.add(exam);
        }

        blichData.setExams(exams);
    }

    /**
     * Insert the given data into realm. Deletes all the old data.
     *
     * @param blichData data to insert.
     */
    private static void loadDataIntoRealm(BlichData blichData) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        //Delete old data
        RealmResults<Hour> hours = realm.where(Hour.class)
                .findAll();
        hours.deleteAllFromRealm();

        RealmResults<Change> changes = realm.where(Change.class)
                .findAll();
        changes.deleteAllFromRealm();

        RealmResults<Event> events = realm.where(Event.class)
                .findAll();
        events.deleteAllFromRealm();

        RealmResults<Exam> exams = realm.where(Exam.class)
                .findAll();
        exams.deleteAllFromRealm();

        //Insert new data
        realm.insert(blichData);
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
