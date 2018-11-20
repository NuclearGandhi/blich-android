/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.StringDef;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.TeacherSubject;
import com.blackcracks.blich.data.exam.Exam;
import com.blackcracks.blich.data.raw.RawData;
import com.blackcracks.blich.data.raw.RawExam;
import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawModifier;
import com.blackcracks.blich.data.raw.RawPeriod;
import com.blackcracks.blich.data.schedule.Lesson;
import com.blackcracks.blich.data.schedule.Modifier;
import com.blackcracks.blich.data.schedule.Period;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import io.realm.RealmList;
import timber.log.Timber;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class ShahafUtils {
    public static final String COMMAND_CLASSES = "classes";
    public static final String COMMAND_SCHEDULE = "schedule";
    public static final String COMMAND_EXAMS = "exams";
    public static final String COMMAND_EVENTS = "events";
    public static final String COMMAND_CHANGES = "changes";
    //BlichData
    private static final String BLICH_BASE_URI =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/ApiHandler.ashx";
    private static final String PARAM_SID = "sid";
    private static final String PARAM_API_KEY = "token";
    private static final String PARAM_COMMAND = "cmd";
    private static final String PARAM_CLASS_ID = "clsid";
    private static final int BLICH_ID = 540211;

    /**
     * Build a URL to Shahaf's servers.
     *
     * @param command a {@link FetchCommand}.
     * @return a {@link URL}.
     */
    public static URL buildUrlFromCommand(Context context, @FetchCommand String command) {
        int classValue = PreferenceUtils.getInstance(context).getInt(R.string.pref_user_class_group_key);

        Uri scheduleUri = Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_CLASS_ID, String.valueOf(classValue))
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();

        return buildURLFromUri(scheduleUri);
    }

    /**
     * Build a URI without {@link #PARAM_CLASS_ID} parameter.
     *
     * @param command a {@link FetchCommand}.
     * @return a {@link Uri}.
     */
    @SuppressWarnings("SameParameterValue")
    public static Uri buildBaseUriFromCommand(@FetchCommand String command) {

        return Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();
    }

    /**
     * Convert a URI into a URL.
     *
     * @param uri the {@link Uri} to convert.
     * @return a {@link URL}.
     */
    public static URL buildURLFromUri(Uri uri) {
        try {

            if (BuildConfig.DEBUG) {
                Timber.d("Building URI: %s", uri.toString());
            }
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            Timber.e(e);
            return null;
        }
    }

    /**
     * Connect to the given url, and return its response.
     *
     * @param url {@link URL} to connect to.
     * @return server response.
     */
    public static String getResponseFromUrl(URL url) throws IOException {
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

    public static RealmList<Period> processScheduleRawData(RawData rawData) {
        Calendar calendar = Calendar.getInstance();
        RealmList<Period> processedData = new RealmList<>();

        for (RawPeriod rawPeriod : rawData.getRawPeriods()) {
            int periodNum = rawPeriod.getHour();
            RealmList<Lesson> lessons = new RealmList<>();
            for (RawLesson rawLesson : rawPeriod.getLessons()) {
                Lesson lesson = new Lesson(
                        rawLesson.getSubject(),
                        rawLesson.getTeacher(), rawLesson.getRoom()
                );
                lessons.add(lesson);
            }

            Period period = new Period(
                    rawPeriod.getDay(),
                    lessons,
                    periodNum);
            processedData.add(period);
        }

        int weekOffest = ScheduleUtils.getWantedWeekOffset();
        for (RawModifier rawModifier : rawData.getRawModifiers()) {
            if (rawModifier.isInWeek(weekOffest)) {
                Modifier modifier = new Modifier(rawModifier);
                List<Integer> missedPeriods = new ArrayList<>();

                for (int i = modifier.getBeginPeriod(); i <= modifier.getEndPeriod(); i++)
                    missedPeriods.add(i);

                for (Period period : processedData) {
                    if (modifier.isIncludedInPeriod(period, calendar)) {
                        missedPeriods.remove((Integer) period.getPeriodNum());

                        period.addChangeTypeColor(modifier.getColor());
                        List<Lesson> lessons = period.getItems();

                        if (!rawModifier.isAReplacer()) {
                            period.removeAllNormalLessons();
                        }

                        boolean isReplacing = false;
                        for (Lesson lesson : lessons) {
                            if (rawModifier.isAReplacer(lesson)) {
                                lesson.setModifier(modifier);
                                isReplacing = true;
                                break;
                            }
                        }

                        if (!isReplacing) {
                            Lesson newLesson = new Lesson(null, null, null);
                            newLesson.setModifier(modifier);
                            lessons.add(newLesson);
                        }
                    }
                }

                for (int periodNum : missedPeriods) {
                    RealmList<Lesson> newLessons = new RealmList<>();
                    Lesson newLesson = new Lesson(null, null, null);
                    newLesson.setModifier(modifier);
                    newLessons.add(newLesson);

                    Period newPeriod = new Period(
                            rawModifier.getDayOfTheWeek(calendar),
                            newLessons,
                            periodNum
                    );

                    processedData.add(newPeriod);
                }
            }
        }

        for (Period period : processedData) {
            period.setFirstLesson(period.getItems().get(0));
            period.getItems().remove(0);
        }

        return processedData;
    }

    public static RealmList<Exam> processExamRawData(RawData rawData) {
        RealmList<Exam> toReturn = new RealmList<>();
        for (RawExam rawExam : RawModifier.extractType(rawData.getRawModifiers(), RawExam.class)) {
            boolean didAdd = false;
            for (int i = 0; i < toReturn.size() && !didAdd; i++) {
                didAdd = toReturn.get(i).addExam(rawExam);
            }
            if (!didAdd) toReturn.add(new Exam(rawExam));
        }

        return toReturn;
    }

    public static RealmList<TeacherSubject> processTeacherSubjectData(RawData rawData) {
        RealmList<TeacherSubject> teacherSubjects = new RealmList<>();

        for (RawPeriod rawPeriod : rawData.getRawPeriods()) {
            for (RawLesson rawLesson : rawPeriod.getLessons()) {
                TeacherSubject teacherSubject = new TeacherSubject(rawLesson);
                if (!teacherSubjects.contains(teacherSubject))
                    teacherSubjects.add(teacherSubject);
            }
        }

        return teacherSubjects;
    }

    @Retention(SOURCE)
    @StringDef({COMMAND_CHANGES, COMMAND_EVENTS, COMMAND_EXAMS, COMMAND_SCHEDULE, COMMAND_CLASSES})
    @interface FetchCommand {
    }
}
