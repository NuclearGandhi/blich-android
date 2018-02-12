/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.preference.PreferenceManager;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichData;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.Constants.Database;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmList;
import timber.log.Timber;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class BlichSyncTask {

    private static final String EVENT_BEGIN_SYNC = "begin_sync";
    private static final String EVENT_END_SYNC = "end_sync";

    private static final String LOG_PARAM_STATUS_SYNC = "status";

    @Retention(SOURCE)
    @IntDef({FETCH_STATUS_SUCCESSFUL, FETCH_STATUS_UNSUCCESSFUL,
            FETCH_STATUS_NO_CONNECTION, FETCH_STATUS_EMPTY_HTML,
            FETCH_STATUS_CLASS_NOT_CONFIGURED})
    public @interface FetchStatus {
    }

    public static final int FETCH_STATUS_SUCCESSFUL = 0;
    public static final int FETCH_STATUS_UNSUCCESSFUL = 1;
    public static final int FETCH_STATUS_NO_CONNECTION = 2;
    public static final int FETCH_STATUS_EMPTY_HTML = 3;
    public static final int FETCH_STATUS_CLASS_NOT_CONFIGURED = 4;


    //Exams
    private static final String EXAMS_BASE_URL =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/MainHtmlExams.aspx?pid=17&mid=6264&layer=0";

    private static final String EXAMS_TABLE_ID = "ChangesList";

    /**
     * Start the fetch.
     * If there is a problem while fetching, send the status in the broadcast.
     */
    public static @FetchStatus
    int syncBlich(Context context) {
        //Log the beginning of sync
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(EVENT_BEGIN_SYNC, Bundle.EMPTY);

        BlichData blichData = new BlichData();
        int status = fetchData(context, blichData);

        //Save in preferences the latest update time
        long currentTime = Calendar.getInstance().getTimeInMillis();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(context.getString(R.string.pref_latest_update_key), currentTime)
                .apply();

        BlichSyncUtils.loadDataIntoRealm(blichData);

        //Log the end of sync
        Bundle bundle = new Bundle();
        bundle.putInt(LOG_PARAM_STATUS_SYNC, status);
        firebaseAnalytics.logEvent(EVENT_END_SYNC, bundle);

        return status;
    }

    private static @FetchStatus
    int fetchData(Context context, BlichData blichData) {
        for(int i = 0; i < 4; i++) {
            String json;

            String command = BlichSyncUtils.COMMAND_SCHEDULE;
            switch (i) {
                case 0: {
                    command = BlichSyncUtils.COMMAND_SCHEDULE;
                    break;
                }
                case 1: {
                    command = BlichSyncUtils.COMMAND_CHANGES;
                    break;
                }
                case 2: {
                    command = BlichSyncUtils.COMMAND_EVENTS;
                    break;
                }
                case 3: {
                    command = BlichSyncUtils.COMMAND_EXAMS;
                    break;
                }
            }

            try {
                json = BlichSyncUtils.getResponseFromUrl(BlichSyncUtils.buildUrlFromCommand(context, command));

                if (json.equals("")) return FETCH_STATUS_EMPTY_HTML;

                switch (command) {
                    case BlichSyncUtils.COMMAND_SCHEDULE: {
                        insertScheduleJsonIntoData(json, blichData);
                        break;
                    }
                    case BlichSyncUtils.COMMAND_CHANGES: {
                        insertChangesJsonIntoData(json, blichData);
                        break;
                    }
                    case BlichSyncUtils.COMMAND_EVENTS: {
                        insertEventsJsonIntoData(json, blichData);
                        break;
                    }
                    case BlichSyncUtils.COMMAND_EXAMS: {
                        insertExamsJsonIntoData(json, blichData);
                        break;
                    }
                }
            } catch (IOException e) {
                Timber.e(e);
                return FETCH_STATUS_UNSUCCESSFUL;
            } catch (JSONException e) {
                Timber.e(e);
                return FETCH_STATUS_UNSUCCESSFUL;
            } catch (BlichSyncUtils.BlichFetchException e) {
                return FETCH_STATUS_CLASS_NOT_CONFIGURED;
            }
        }

        return FETCH_STATUS_SUCCESSFUL;
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
                lesson.setChangeType(Database.TYPE_NORMAL);

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

    private static Date parseDate(String jsonDate) {
        int firstCut = jsonDate.indexOf("(") + 1;
        int lastCut = jsonDate.indexOf(")");
        long timeInMillis = Long.parseLong(jsonDate.substring(firstCut, lastCut));
        return new Date(timeInMillis);
    }
}
