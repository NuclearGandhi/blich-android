package com.blackcracks.blich.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.sync.BlichSyncTask;
import com.blackcracks.blich.sync.BlichSyncUtils;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.widget.BlichWidgetProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.DynamicRealm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmSchema;
import timber.log.Timber;

public class Utilities {

    public static boolean isThereNetworkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isFirstLaunch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ChooseClassDialogFragment.PREF_IS_FIRST_LAUNCH_KEY, true);
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    //Preferences
    public static String getPrefString(Context context,
                                       String key,
                                       String defaultValue,
                                       boolean isUri) {
        String returnString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defaultValue);
        if (isUri) return returnString;
        else return returnString.replace("/", "");
    }

    public static String getPrefString(Context context,
                                       int prefKey) {
        String key = Preferences.getKey(context, prefKey);
        String defaultValue = (String) Preferences.getDefault(context, prefKey);

        return getPrefString(context, key, defaultValue, false);
    }

    public static boolean getPrefBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, defaultValue);
    }

    public static boolean getPrefBoolean(Context context, int prefKey) {
        String key = Preferences.getKey(context, prefKey);
        boolean defaultValue = (boolean) Preferences.getDefault(context, prefKey);

        return getPrefBoolean(context, key, defaultValue);
    }

    public static int getPrefInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, defaultValue);
    }


    public static long getTimeInMillisFromDate(String date) {

        Locale locale = new Locale("iw");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        Date examDate = null;
        try {
            examDate = dateFormat.parse(date);
        } catch (ParseException e) {
            Timber.d(e, e.getMessage());
        }

        return examDate.getTime();
    }

    public static void initializeBlichDataUpdater(Context context, View view) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        updateBlichData(context, view);
    }

    //Call BlichSyncTask to begin a sync
    public static void updateBlichData(Context context, View view) {

        boolean isConnected = false;
        boolean isFetching = getPrefBoolean(
                context,
                Preferences.PREF_IS_SYNCING_KEY
        );
        if (!isFetching) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(context.getString(R.string.pref_is_syncing_key), true)
                    .apply();
            isConnected = Utilities.isThereNetworkConnection(context);
            if (isConnected) {
                BlichSyncUtils.startImmediateSync(context);
            } else {
                onSyncFinished(context, view, BlichSyncTask.FETCH_STATUS_NO_CONNECTION, null);
            }
        }
        if (BuildConfig.DEBUG) {
            Timber.d("updateBlichData() called" +
                    ", isFetching = " + isFetching +
                    ", isConnected = " + isConnected);
        }
    }

    //Callback from BlichSyncTask's sync
    @SuppressLint("SwitchIntDef")
    public static void onSyncFinished(final Context context,
                                      final View view,
                                      @BlichSyncTask.FetchStatus int status,
                                      @Nullable FragmentManager fragmentManager) {

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        if (status == BlichSyncTask.FETCH_STATUS_SUCCESSFUL) {
            Snackbar.make(view,
                    R.string.snackbar_fetch_successful,
                    Snackbar.LENGTH_LONG)
                    .show();

        } else if (status == BlichSyncTask.FETCH_STATUS_CLASS_NOT_CONFIGURED) {

            if (fragmentManager != null) {
                ChooseClassDialogFragment dialogFragment = new ChooseClassDialogFragment();
                dialogFragment.show(fragmentManager, "choose_class");
                dialogFragment.setOnDestroyListener(new ChooseClassDialogFragment.OnDestroyListener() {
                    @Override
                    public void onDestroy(Context context) {
                        //Start the periodic syncing of
                        BlichSyncUtils.initialize(context);
                        Utilities.initializeBlichDataUpdater(context, view);
                    }
                });
            } else {
                throw new NullPointerException("A non-null fragment manager is required " +
                        "in case the user's class isn't configured");
            }

        } else {
            @SuppressLint("InflateParams")
            View dialogView = LayoutInflater.from(context).inflate(
                    R.layout.dialog_fetch_failed,
                    null);

            @StringRes int titleString;
            @StringRes int messageString;
            switch (status) {
                case BlichSyncTask.FETCH_STATUS_NO_CONNECTION: {
                    titleString = R.string.dialog_fetch_no_connection_title;
                    messageString = R.string.dialog_fetch_no_connection_message;
                    break;
                }
                case BlichSyncTask.FETCH_STATUS_EMPTY_HTML: {
                    titleString = R.string.dialog_fetch_empty_html_title;
                    messageString = R.string.dialog_fetch_empty_html_message;
                    break;
                }
                default:
                    titleString = R.string.dialog_fetch_unsuccessful_title;
                    messageString = R.string.dialog_fetch_unsuccessful_message;
            }
            TextView title = dialogView.findViewById(R.id.dialog_title);
            title.setText(titleString);
            TextView message = dialogView.findViewById(R.id.dialog_message);
            message.setText(messageString);

            new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton(R.string.dialog_try_again,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateBlichData(context, view);
                                }
                            })
                    .setNegativeButton(R.string.dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                    .show();
        }
    }

    public static void setLocaleToHebrew(Context context) {
        //Change locale to hebrew
        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.getApplicationContext().createConfigurationContext(config);

    }


    public static void updateWidget(Context context) {
        ComponentName widget = new ComponentName(context, BlichWidgetProvider.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        widgetManager.updateAppWidget(widget, views);
    }

    public static class Realm {

        public static void setUpRealm(Context context) {
            io.realm.Realm.init(context);
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .schemaVersion(6)
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
        public static <E extends RealmModel> RealmQuery<E> getFilteredQuery(
                io.realm.Realm realm,
                Context context,
                java.lang.Class<E> clazz,
                int day) {

            String teacherFilter = Utilities.getPrefString(
                    context,
                    Preferences.PREF_FILTER_SELECT_KEY);
            String[] teacherSubjects = teacherFilter.split(";");

            String dayAttribute;
            switch (clazz.getSimpleName()) {
                case "Change": {
                    dayAttribute = "day";
                    break;
                }
                default: {
                    dayAttribute = "owners.day";
                    break;
                }
            }
            RealmQuery<E> results = realm.where(clazz)
                    .equalTo(dayAttribute, day) //Inverse Relationship
                    .and()
                    .beginGroup()
                    //Set an impossible case for easier code writing
                    .equalTo("subject", "oghegijd39");

            for (String teacherSubject :
                    teacherSubjects) {
                if (teacherSubject.equals("")) break;

                String[] arr = teacherSubject.split(",");
                String teacher = arr[0];
                String subject = arr[1];

                results.or()
                        .beginGroup()
                        .equalTo("teacher", teacher)
                        .and()
                        .equalTo("subject", subject)
                        .endGroup();
            }

            results.endGroup();

            return results;
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

            public @Nullable Change getChange(int hour, Lesson lesson) {
                List<Change> hourChanges = getChanges(hour);
                for(int i = 0; i < hourChanges.size(); i++) {
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

    public static class News {

        public static void setIsFetchingForCategory(Context context, int category, boolean isFetching) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                    context.getResources().getString(R.string.pref_is_fetching_news_key) + category,
                    isFetching
            ).apply();
        }

        public static boolean getIsFetchingForCategory(Context context, int category) {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    context.getResources().getString(R.string.pref_is_fetching_news_key) + category,
                    false
            );
        }

        public static void resetIsFetchingPreferences(Context context) {
            for (int i = 0; i <= 4; i++) {
                setIsFetchingForCategory(context, i, false);
            }
        }

        public static void setPreferenceLatestUpdateForCategory(Context context,
                                                                int category,
                                                                long epoch) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(
                    context.getResources().getString(R.string.pref_latest_update_key) + category,
                    epoch
            ).apply();
        }

        public static long getLatestUpdateForCategory(Context context, int category) {
            return PreferenceManager.getDefaultSharedPreferences(context).getLong(
                    context.getResources().getString(R.string.pref_latest_update_key) + category,
                    0
            );
        }

        public static String getActionForCategory(int category) {
            return Constants.IntentConstants.ACTION_FETCH_NEWS_CALLBACK + category;
        }
    }

    public static class Schedule {

        public static int getWantedDayOfTheWeek() {

            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (hour >= 18 && day != 7)
                day++; //Move to the next day if it is later than 18:00, unless it is Saturday.
            if (day == 7) day = 1; //If it is Saturday, set day to 1 (Sunday).
            return day;
        }
    }

    public static class Class {

        public static String getCurrentClass(Context context) {

            @Preferences.PrefIntKeys int intKey = Preferences.PREF_CLASS_PICKER_KEY;
            String key = Preferences.getKey(context, intKey);

            String defaultValue = (String) Preferences.getDefault(context, intKey);

            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(
                            key,
                            defaultValue.replace("/", ""));
        }
    }
}
