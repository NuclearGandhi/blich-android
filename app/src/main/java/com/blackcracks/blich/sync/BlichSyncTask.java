/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.preference.PreferenceManager;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichData;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.Utilities;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
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


    //BlichData
    private static final String BLICH_BASE_URI =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/ApiHandler.ashx";

    private static final String PARAM_SID = "sid";
    private static final String PARAM_API_KEY = "token";
    private static final String PARAM_COMMAND = "cmd";
    private static final String PARAM_CLASS_ID = "clsid";


    private static final int BLICH_ID = 540211;
    private static final String COMMAND_CLASSES = "classes";
    private static final String COMMAND_SCHEDULE = "schedule";
    private static final String COMMAND_EXAMS = "exams";
    private static final String COMMAND_EVENTS = "events";
    private static final String COMMAND_CHANGES = "changes";

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
        if (status == FETCH_STATUS_SUCCESSFUL) syncExams(context);

        //Save in preferences the latest update time
        long currentTime = Calendar.getInstance().getTimeInMillis();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(context.getString(R.string.pref_latest_update_key), currentTime)
                .apply();

        loadDataIntoRealm(blichData);

        //Log the end of sync
        Bundle bundle = new Bundle();
        bundle.putInt(LOG_PARAM_STATUS_SYNC, status);
        firebaseAnalytics.logEvent(EVENT_END_SYNC, bundle);

        return status;
    }

    private static @FetchStatus
    int fetchData(Context context, BlichData blichData) {
        for(int i = 0; i < 2; i++) {
            String json;

            String command = COMMAND_SCHEDULE;
            switch (i) {
                case 0: {
                    command = COMMAND_SCHEDULE;
                    break;
                }
                case 1: {
                    command = COMMAND_CHANGES;
                    break;
                }
            }

            try {
                json = getResponseFromUrl(buildUrlFromCommand(context, command));

                if (json.equals("")) return FETCH_STATUS_EMPTY_HTML;

                switch (command) {
                    case COMMAND_SCHEDULE: {
                        insertScheduleJsonIntoData(json, blichData);
                        break;
                    }
                    case COMMAND_CHANGES: {
                        insertChangesJsonIntoData(json, blichData);
                        break;
                    }
                }
            } catch (IOException e) {
                Timber.e(e);
                return FETCH_STATUS_UNSUCCESSFUL;
            } catch (JSONException e) {
                Timber.e(e);
                return FETCH_STATUS_UNSUCCESSFUL;
            } catch (BlichFetchException e) {
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

            change.setChangeType(jsonChange.getString(Database.JSON_STRING_CHANGE_TYPE));
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

    private static @FetchStatus
    int syncExams(Context context) {

        int classValue;
        BufferedReader reader = null;
        String html = "";

        final String CLASS_VALUE_PARAM = "cls";

        //Get the exams html from Blich's site
        try {
            classValue = getClassValue(context);
            Uri baseUri = Uri.parse(EXAMS_BASE_URL).buildUpon()
                    .appendQueryParameter(CLASS_VALUE_PARAM, String.valueOf(classValue))
                    .build();

            URL url = new URL(baseUri.toString());
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setDoOutput(true);

            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            html = stringBuilder.toString();

        } catch (BlichFetchException e) {
            Timber.e(e, "Error while trying to get user's class value");
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Timber.e(e, "Error closing stream");
                }
            }
        }

        if (html.equals(""))
            return FETCH_STATUS_EMPTY_HTML;


        //Parse the html
        Document document = Jsoup.parse(html);
        Elements exams = document.getElementById(EXAMS_TABLE_ID).getElementsByTag("tr");

        List<ContentValues> contentValues = new ArrayList<>();
        int mPreviousMonth = 0;

        //Parse the table rows in the html from the second row
        //(first row is the header of each column)
        for (int i = 1; i < exams.size(); i++) {

            Element exam = exams.get(i);
            Elements innerData = exam.getElementsByTag("td");
            String date = innerData.get(0).text();
            String subject = innerData.get(1).text();
            String teachers = innerData.get(2).text();

            Calendar calendar = Calendar.getInstance();
            long dateInMillis = Utilities.getTimeInMillisFromDate(date);
            calendar.setTimeInMillis(dateInMillis);

            int currentMonth = calendar.get(Calendar.MONTH);

            if (mPreviousMonth != currentMonth) {
                ContentValues monthDivider = new ContentValues();
                monthDivider.put(BlichContract.ExamsEntry.COL_TEACHER, "wut");
                monthDivider.put(BlichContract.ExamsEntry.COL_DATE, dateInMillis);
                monthDivider.put(BlichContract.ExamsEntry.COL_SUBJECT, "" + currentMonth);

                contentValues.add(monthDivider);
            }

            mPreviousMonth = currentMonth;

            ContentValues row = new ContentValues();
            row.put(BlichContract.ExamsEntry.COL_DATE, dateInMillis);
            row.put(BlichContract.ExamsEntry.COL_SUBJECT, subject);
            row.put(BlichContract.ExamsEntry.COL_TEACHER, teachers);

            contentValues.add(row);
        }

        //Delete the whole exams table
        context.getContentResolver().delete(BlichContract.ExamsEntry.CONTENT_URI, null, null);
        //Repopulate the table with updated data
        context.getContentResolver().bulkInsert(
                BlichContract.ExamsEntry.CONTENT_URI,
                contentValues.toArray(new ContentValues[contentValues.size()]));

        return FETCH_STATUS_SUCCESSFUL;
    }

    private static void loadDataIntoRealm(BlichData blichData) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        //Delete old data
        RealmResults<Hour> hours = realm.where(Hour.class)
                .findAll();
        hours.deleteAllFromRealm();

        //Insert new data
        realm.insert(blichData);
        realm.commitTransaction();
        realm.close();
    }

    private static int getClassValue(Context context)
            throws BlichFetchException {
        String currentClass = Utilities.Class.getCurrentClass(context);
        String selection;
        String[] selectionArgs;

        if (currentClass.contains("'")) { //Normal class syntax
            selection = BlichContract.ClassEntry.COL_GRADE + " = ? AND " +
                    BlichContract.ClassEntry.COL_GRADE_INDEX + " = ?";
            selectionArgs = currentClass.split("'");
        } else { //Abnormal class syntax
            selection = BlichContract.ClassEntry.COL_GRADE + " = ?";
            selectionArgs = new String[]{currentClass};
        }


        Cursor cursor = context.getContentResolver().query(
                BlichContract.ClassEntry.CONTENT_URI,
                new String[]{BlichContract.ClassEntry.COL_CLASS_INDEX},
                selection,
                selectionArgs,
                null);

        int classValue;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                classValue = cursor.getInt(0);
            } else {
                throw new BlichFetchException("Can't get the user's class. " +
                        "Did the user configure his class?");
            }
        } else {
            throw new NullPointerException("Queried cursor is null");
        }

        cursor.close();
        return classValue;
    }

    private static URL buildUrlFromCommand(Context context, String command)
            throws BlichFetchException {
        int classValue = getClassValue(context);

        Uri scheduleUri = Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_CLASS_ID, String.valueOf(classValue))
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();

        if (BuildConfig.DEBUG) Timber.d("Building URI: %s", scheduleUri.toString());

        try {
            return new URL(scheduleUri.toString());
        } catch (MalformedURLException e) {
            Timber.e(e);
            return null;
        }
    }

    private static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection scheduleConnection = (HttpURLConnection) url.openConnection();

        InputStream in = scheduleConnection.getInputStream();

        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\A");

        boolean hasInput = scanner.hasNext();
        String response = null;
        if (hasInput) {
            response = scanner.next();
        }
        scanner.close();
        scheduleConnection.disconnect();

        return response;
    }

    public static class BlichFetchException extends Exception {
        public BlichFetchException(String message) {
            super(message);
        }
    }
}
