/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import com.blackcracks.blich.R;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class Constants {

    public static class Database {
        public static final String JSON_ARRAY_HOURS = "Schedule";
        public static final String JSON_INT_CLASS_ID = "ClassId";

        public static final String JSON_INT_DAY = "Day";
        public static final String JSON_INT_HOUR = "Hour";
        public static final String JSON_ARRAY_LESSONS = "Lessons";

        public static final String JSON_STRING_SUBJECT = "Subject";
        public static final String JSON_STRING_TEACHER = "Teacher";
        public static final String JSON_STRING_ROOM = "Room";
        public static final String JSON_STRING_DATE = "Date";

        public static final String JSON_ARRAY_CHANGES = "Changes";
        public static final String JSON_OBJECT_STUDY_GROUP = "StudyGroup";
        public static final String JSON_STRING_CHANGE_TYPE = "ChangeType";
        public static final String JSON_INT_NEW_HOUR = "NewHour";
        public static final String JSON_STRING_NEW_TEACHER = "NewTeacher";
        public static final String JSON_STRING_NEW_ROOM = "NewRoom";

        public static final String JSON_ARRAY_EVENTS = "Events";
        public static final String JSON_NAME = "Name";
        public static final String JSON_INT_BEGIN_HOUR = "FromHour";
        public static final String JSON_INT_END_HOUR = "ToHour";

        public static final String JSON_ARRAY_EXAMS = "Exams";

        public static final String JSON_ARRAY_CLASSES = "Classes";
        public static final String JSON_INT_ID = "Id";
        public static final String JSON_STRING_NAME = "Name";
        public static final String JSON_INT_GRADE = "Grade";
        public static final String JSON_INT_NUMBER = "Number";

        public static final String TYPE_NEW_TEACHER = "NewTeacher";
        public static final String TYPE_NEW_HOUR = "HourMove";
        public static final String TYPE_NEW_ROOM = "NewRoom";
        public static final String TYPE_EXAM = "Exam";
        public static final String TYPE_CANCELED = "FreeLesson";
        public static final String TYPE_EVENT = "Event";
        public static final String TYPE_NORMAL = "Normal";

    }

    /**
     * Preference constants and methods to easily obtain preferences keys and default values.
     */
    public static class Preferences {

        @Retention(SOURCE)
        @IntDef({PREF_USER_CLASS_GROUP_KEY, PREF_NOTIFICATION_TOGGLE_KEY, PREF_NOTIFICATION_SOUND_KEY,
                PREF_FILTER_TOGGLE_KEY, PREF_FILTER_SELECT_KEY, PREF_THEME_BACKGROUND_KEY,
                PREF_IS_SYNCING_KEY, PREF_APP_VERSION_KEY})
        @interface PrefIntKeys {}

        public static final int PREF_USER_CLASS_GROUP_KEY = 0;

        public static final int PREF_NOTIFICATION_TOGGLE_KEY = 1;
        public static final int PREF_NOTIFICATION_SOUND_KEY = 2;

        public static final int PREF_FILTER_TOGGLE_KEY = 3;
        public static final int PREF_FILTER_SELECT_KEY = 4;

        public static final int PREF_THEME_BACKGROUND_KEY = 5;

        public static final int PREF_IS_SYNCING_KEY = 100;
        public static final int PREF_APP_VERSION_KEY = 101;


        private static final String PREF_NOTIFICATION_SOUND_DEFAULT =
                Settings.System.DEFAULT_NOTIFICATION_URI.toString();

        /**
         * Get a preference key
         *
         * @param key a {@link PrefIntKeys}.
         * @return the corresponding preference string key.
         */
        public static String getKey(Context context, @PrefIntKeys int key) {

            @StringRes int resKey = -1;
            switch (key) {
                case PREF_USER_CLASS_GROUP_KEY: {
                    resKey = R.string.pref_user_class_group_key;
                    break;
                }
                case PREF_NOTIFICATION_TOGGLE_KEY: {
                    resKey = R.string.pref_notification_toggle_key;
                    break;
                }
                case PREF_NOTIFICATION_SOUND_KEY: {
                    resKey = R.string.pref_notification_sound_key;
                    break;
                }
                case PREF_FILTER_TOGGLE_KEY: {
                    resKey = R.string.pref_filter_toggle_key;
                    break;
                }
                case PREF_FILTER_SELECT_KEY: {
                    resKey = R.string.pref_filter_select_key;
                    break;
                }
                case PREF_THEME_BACKGROUND_KEY: {
                    resKey = R.string.pref_theme_background_key;
                    break;
                }
                case PREF_IS_SYNCING_KEY: {
                    resKey = R.string.pref_is_syncing_key;
                    break;
                }
                case PREF_APP_VERSION_KEY: {
                    resKey = R.string.pref_app_version_key;
                    break;
                }
            }
            return context.getString(resKey);
        }

        /**
         * Get the default value for the given {@link PrefIntKeys}.
         *
         * @param key a {@link PrefIntKeys}.
         * @return the corresponding default value.
         */
        public static Object getDefault(Context context, @PrefIntKeys int key) {
            switch (key) {
                case PREF_USER_CLASS_GROUP_KEY: {
                    return context.getResources().getInteger(R.integer.pref_user_class_group_default);
                }
                case PREF_NOTIFICATION_TOGGLE_KEY: {
                    return context.getResources().getBoolean(R.bool.pref_notification_toggle_default);
                }
                case PREF_NOTIFICATION_SOUND_KEY: {
                    return PREF_NOTIFICATION_SOUND_DEFAULT;
                }
                case PREF_FILTER_TOGGLE_KEY: {
                    return context.getResources().getBoolean(R.bool.pref_filter_toggle_default);
                }
                case PREF_FILTER_SELECT_KEY: {
                    return "";
                }
                case PREF_THEME_BACKGROUND_KEY: {
                    return false;
                }
                case PREF_IS_SYNCING_KEY: {
                    return true;
                }
                case PREF_APP_VERSION_KEY: {
                    return context.getResources().getInteger(R.integer.pref_user_class_group_default);
                }
            }
            return null;
        }

    }

    public static class IntentConstants {

        public static final String ACTION_SYNC_CALLBACK = "sync_callback";

        public static final String EXTRA_FETCH_STATUS = "extra_fetch_status";
    }
}
