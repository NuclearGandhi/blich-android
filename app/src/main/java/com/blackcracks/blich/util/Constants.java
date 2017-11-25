package com.blackcracks.blich.util;

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import com.blackcracks.blich.R;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class Constants {

    /**
     * Starting from 14, because {@link com.blackcracks.blich.adapter.ScheduleAdapter} uses 0 - 13
     */
    public static final int EXAMS_LOADER_ID = 14;
    public static final int SCHEDULE_LOADER_ID = 15;
    public static final int NEWS_LOADER_ID = 16;

    public static class Preferences {

        //Preference keys
        @Retention(SOURCE)
        @IntDef({PREF_CLASS_PICKER_KEY, PREF_NOTIFICATION_TOGGLE_KEY, PREF_NOTIFICATION_SOUND_KEY,
            PREF_FILTER_TOGGLE_KEY, PREF_FILTER_SELECT_KEY})
        public @interface PrefIntKeys {}
        public static final int PREF_CLASS_PICKER_KEY = 0;
        public static final int PREF_NOTIFICATION_TOGGLE_KEY = 1;
        public static final int PREF_NOTIFICATION_SOUND_KEY = 2;
        public static final int PREF_FILTER_TOGGLE_KEY = 3;
        public static final int PREF_FILTER_SELECT_KEY = 4;

        public static String getKey(Context context, @PrefIntKeys int key) {

            @StringRes int resKey = -1;
            switch (key) {
                case PREF_CLASS_PICKER_KEY: {
                    resKey = R.string.pref_class_picker_key;
                    break;
                }
                case PREF_NOTIFICATION_TOGGLE_KEY:{
                    resKey = R.string.pref_notification_toggle_key;
                    break;
                }
                case PREF_NOTIFICATION_SOUND_KEY:{
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
            }
            return context.getString(resKey);
        }

        private static final String PREF_NOTIFICATION_SOUND_DEFAULT =
                Settings.System.DEFAULT_NOTIFICATION_URI.toString();

        public static Object getDefault(Context context, @PrefIntKeys int key) {
            switch (key) {
                case PREF_CLASS_PICKER_KEY:
                    return context.getString(R.string.pref_class_picker_default);

                case PREF_NOTIFICATION_TOGGLE_KEY:
                    return context.getResources().getBoolean(R.bool.pref_notification_toggle_default);

                case PREF_NOTIFICATION_SOUND_KEY:
                    return PREF_NOTIFICATION_SOUND_DEFAULT;
                case PREF_FILTER_TOGGLE_KEY:
                    return context.getResources().getBoolean(R.bool.pref_filter_toggle_default);
                case PREF_FILTER_SELECT_KEY:
                    return "";
            }
            return null;
        }

    }

    public static class IntentConstants {

        public static final String ACTION_SYNC_CALLBACK = "sync_callback";
        public static final String ACTION_FETCH_NEWS_CALLBACK = "fetch_news_callback";


        public static final String EXTRA_FETCH_STATUS = "extra_fetch_status";
        public static final String EXTRA_NEWS_CATEGORY = "extra_category";

        public static final String EXTRA_ARTICLE_TITLE = "article_title";
        public static final String EXTRA_ARTICLE_BODY = "article_body";
    }

    public static class Widget {
        public static final String EXTRA_WIDGET_ID = "widget_id";
    }
}
