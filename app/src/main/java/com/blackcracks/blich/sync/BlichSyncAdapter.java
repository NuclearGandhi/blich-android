package com.blackcracks.blich.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.BlichContract.ClassEntry;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;
import com.blackcracks.blich.data.BlichContract.LessonEntry;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.Constants.IntentConstants;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.Utilities;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;


public class BlichSyncAdapter extends AbstractThreadedSyncAdapter {

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

    private static final String SYNC_IS_PERIODIC = "is_periodic";

    private static final int NOTIFICATION_UPDATE_ID = 1;

    private static final String LOG_TAG = BlichSyncAdapter.class.getSimpleName();

    //Schedule
    private static final String SOURCE_URL =
            "http://blich.iscool.co.il/tabid/2117/language/he-IL/Default.aspx";

    private static final String VIEW_STATE = "__VIEWSTATE";
    private static final String LAST_FOCUS = "__LAS" +
            "TFOCUS";
    private static final String EVENT_ARGUMENT = "__EVENTARGUMENT";
    private static final String EVENT_TARGET = "__EVENTTARGET";

    private static final String SELECTOR_NAME = "dnn$ctr7919$TimeTableView$ClassesList";

    private static final String SCHEDULE_BUTTON_NAME = "dnn$ctr7919$TimeTableView$btnChangesTable";

    private static final String SCHEDULE_TABLE_ID = "dnn_ctr7919_TimeTableView_PlaceHolder";

    private static final String CELL_CLASS = "TTCell";
    private static final String CANCELED_LESSON_CLASS = "TableFreeChange";
    private static final String CHANGED_LESSON_CLASS = "TableFillChange";
    private static final String EXAM_LESSON_CLASS = "TableExamChange";
    private static final String EVENT_LESSON_CLASS = "TableEventChange";

    //Exams
    private static final String EXAMS_BASE_URL =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/MainHtmlExams.aspx?pid=17&mid=6264&layer=0";

    private static final String EXAMS_TABLE_ID = "ChangesList";


    private final Context mContext = getContext();
    private List<Lesson> mLessonNotificationList = new ArrayList<>();

    BlichSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    public static void initializeSyncAdapter(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BlichUpdateReceiver.class);
        PendingIntent mornIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                0);

        PendingIntent nightIntent = PendingIntent.getBroadcast(context,
                100,
                intent,
                0);

        ComponentName receiver = new ComponentName(context, StartPeriodicSyncBootReceiver.class);
        PackageManager pm = context.getPackageManager();


        int intKey = Preferences.PREF_NOTIFICATION_TOGGLE_KEY;
        String prefKey = Preferences.getKey(context, intKey);
        boolean prefDefault = (boolean) Preferences.getDefault(context, intKey);
        boolean isPeriodicSyncOn = Utilities.getPrefBoolean(context,
                prefKey,
                prefDefault);
        if (isPeriodicSyncOn) {
            Log.d(LOG_TAG, "Initializing sync: on");
            ContentResolver.setSyncAutomatically(
                    getSyncAccount(context),
                    context.getString(R.string.content_authority),
                    true);
            configurePeriodicSync(alarmManager, mornIntent, nightIntent);

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            Log.d(LOG_TAG, "Initializing sync: off");
            ContentResolver.setSyncAutomatically(
                    getSyncAccount(context),
                    context.getString(R.string.content_authority),
                    false);
            alarmManager.cancel(mornIntent);
            alarmManager.cancel(nightIntent);

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    private static Account getSyncAccount(Context context) {

        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
        }
        return newAccount;
    }

    private static void configurePeriodicSync(AlarmManager alarmManager,
                                              PendingIntent mornIntent,
                                              PendingIntent nightIntent) {
        Calendar nightNotify = Calendar.getInstance();
        nightNotify.setTimeInMillis(System.currentTimeMillis());
        nightNotify.set(Calendar.HOUR_OF_DAY, 21);
        nightNotify.set(Calendar.MINUTE, 30);
        Log.d(LOG_TAG, "Before Night notify: " + nightNotify.getTimeInMillis());
        if (nightNotify.getTimeInMillis() < System.currentTimeMillis()) {
            nightNotify.add(Calendar.DAY_OF_MONTH, 1);
        }

        Log.d(LOG_TAG, "After Night notify: " + nightNotify.getTimeInMillis());

        Calendar mornNotify = Calendar.getInstance();
        mornNotify.setTimeInMillis(System.currentTimeMillis());
        mornNotify.set(Calendar.HOUR_OF_DAY, 7);
        mornNotify.set(Calendar.MINUTE, 0);
        Log.d(LOG_TAG, "Before Morn notify: " + mornNotify.getTimeInMillis());

        if (mornNotify.getTimeInMillis() < System.currentTimeMillis()) {
            mornNotify.add(Calendar.DAY_OF_MONTH, 1);
        }

        Log.d(LOG_TAG, "After Morn notify: " + mornNotify.getTimeInMillis());

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                nightNotify.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                mornIntent);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                mornNotify.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                nightIntent);

    }

    public static void syncImmediately(Context context) {

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(
                getSyncAccount(context),
                context.getString(R.string.content_authority),
                bundle);
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle bundle,
                              String s,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {
        boolean periodic = bundle.containsKey(SYNC_IS_PERIODIC) && bundle.getBoolean(SYNC_IS_PERIODIC);

        if (periodic) {
            Log.d(LOG_TAG, "Syncing: Periodic");
        } else {
            Log.d(LOG_TAG, "Syncing: Manual");
        }

        /*
        Start the fetch.
        If there is a problem while fetching, send the status in the broadcast.
         */
        int status;
        if ((status = fetchSchedule()) != FETCH_STATUS_SUCCESSFUL ||
                (status = fetchExams()) != FETCH_STATUS_SUCCESSFUL)
            sendBroadcast(status);
        else {
            sendBroadcast(FETCH_STATUS_SUCCESSFUL);

            long currentTime = Calendar.getInstance().getTimeInMillis();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .putLong(getContext().getString(R.string.pref_latest_update_key), currentTime)
                    .apply();

        }

        if (periodic) {
            notifyUser();
        }
    }

    private void sendBroadcast(@FetchStatus int status) {
        Intent intent = new Intent(IntentConstants.ACTION_SYNC_CALLBACK);
        intent.putExtra(IntentConstants.EXTRA_FETCH_STATUS, status);
        LocalBroadcastManager.getInstance(getContext())
                .sendBroadcast(intent);

        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putInt(getContext().getString(R.string.pref_fetch_status_key), status)
                .apply();
    }

    private
    @FetchStatus
    int fetchSchedule() {

        int classValue = 0;
        BufferedReader reader = null;
        String classHtml = "";

        try {

            classValue = getClassValue();

            /*
            get the html
             */
            URL viewStateUrl = new URL(SOURCE_URL);
            URLConnection viewStateCon = viewStateUrl.openConnection();
            viewStateCon.setConnectTimeout(10000);
            viewStateCon.setDoOutput(true);

            reader = new BufferedReader(new InputStreamReader(viewStateCon.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String html = builder.toString();

            /*
            parse the html to get the view state
             */
            Document document = Jsoup.parse(html);
            Element viewState = document.getElementById(VIEW_STATE);

            String viewStateValue;
            if (viewState != null) {
                viewStateValue = viewState.attr("value");
            } else {
                return FETCH_STATUS_EMPTY_HTML;
            }

            /*
            create a POST request
             */
            URL scheduleUrl = new URL(SOURCE_URL);
            HttpURLConnection scheduleCon = (HttpURLConnection) scheduleUrl.openConnection();
            scheduleCon.setConnectTimeout(10000);
            scheduleCon.setDoOutput(true);

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair(EVENT_TARGET, SCHEDULE_BUTTON_NAME));
            nameValuePairs.add(new BasicNameValuePair(EVENT_ARGUMENT, ""));
            nameValuePairs.add(new BasicNameValuePair(SELECTOR_NAME, Integer.toString(classValue)));
            nameValuePairs.add(new BasicNameValuePair(VIEW_STATE, viewStateValue));
            nameValuePairs.add(new BasicNameValuePair(LAST_FOCUS, ""));

            OutputStreamWriter classWriter = new OutputStreamWriter(scheduleCon.getOutputStream());
            classWriter.write(getQuery(nameValuePairs));
            classWriter.flush();

            reader = new BufferedReader(new InputStreamReader(scheduleCon.getInputStream()));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            classHtml = builder.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);

        } catch (BlichFetchException e) { //The user's class isn't configured properly
            //Let the user choose his class again
            return FETCH_STATUS_CLASS_NOT_CONFIGURED;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        if (classHtml.equals("")) {
            return FETCH_STATUS_EMPTY_HTML;
        }
        Document document = Jsoup.parse(classHtml);
        Element table = document.getElementById(SCHEDULE_TABLE_ID);
        if (table == null) {
            return FETCH_STATUS_EMPTY_HTML;
        }
        Elements lessons = table.getElementsByClass(CELL_CLASS);

        List<ContentValues> scheduleValues = new ArrayList<>();
        List<ContentValues> lessonValues = new ArrayList<>();
        for (int i = 6; i < lessons.size(); i++) {
            int row = i / 6;
            int column = i % 6 + 1;
            Element lesson = lessons.get(i);
            Elements divs = lesson.getElementsByTag("div");

            Elements trs = lesson.getElementsByTag("tr");
            Elements tds = new Elements();
            for (Element tr : trs) {
                tds.add(tr.getElementsByTag("td").get(0));
            }

            divs.addAll(tds);

            char[] events = {'f', 'f', 'f', 'f'};

            for (int k = 0; k < divs.size(); k++) {
                Element div = divs.get(k);
                String html = div.html();
                String[] text = html.split("</b>");

                String subject, classroom, teacher, lessonType;

                subject = text[0].replace("<b>", "").replace("<br>", " ");
                subject = Parser.unescapeEntities(subject, false);

                //TODO Improve this shitty code
                if (text.length == 2) {
                    text = text[1].split("<br>");

                    if (text.length == 2) {
                        classroom = text[0].replace("&nbsp;&nbsp;", "").replace("(", "").replace(")", "");
                        teacher = text[1];
                    } else {
                        classroom = "";
                        teacher = "";
                    }
                } else {
                    classroom = " ";
                    teacher = " ";
                }

                switch (div.attr("class")) {
                    case CANCELED_LESSON_CLASS: {
                        lessonType = LessonEntry.LESSON_TYPE_CANCELED;
                        if (events[0] == 'f') events[0] = 't';
                        break;
                    }
                    case CHANGED_LESSON_CLASS: {
                        lessonType = LessonEntry.LESSON_TYPE_CHANGED;
                        if (events[1] == 'f') events[1] = 't';
                        break;
                    }
                    case EXAM_LESSON_CLASS: {
                        lessonType = LessonEntry.LESSON_TYPE_EXAM;
                        if (events[2] == 'f') events[2] = 't';
                        break;
                    }
                    case EVENT_LESSON_CLASS: {
                        lessonType = LessonEntry.LESSON_TYPE_EVENT;
                        if (events[3] == 'f') events[3] = 't';
                        break;
                    }
                    default: {
                        lessonType = LessonEntry.LESSON_TYPE_NORMAL;
                    }
                }

                addLessonToNotificationList(classValue,
                        column,
                        row,
                        subject,
                        lessonType);

                ContentValues lessonValue = new ContentValues();
                lessonValue.put(LessonEntry.COL_DAY, column);
                lessonValue.put(LessonEntry.COL_HOUR, row);
                lessonValue.put(LessonEntry.COL_LESSON_NUM, k);
                lessonValue.put(LessonEntry.COL_SUBJECT, subject);
                lessonValue.put(LessonEntry.COL_CLASSROOM, classroom);
                lessonValue.put(LessonEntry.COL_TEACHER, teacher);
                lessonValue.put(LessonEntry.COL_LESSON_TYPE, lessonType);

                lessonValues.add(lessonValue);
            }

            ContentValues scheduleValue = new ContentValues();
            scheduleValue.put(ScheduleEntry.COL_DAY, column);
            scheduleValue.put(ScheduleEntry.COL_HOUR, row);
            scheduleValue.put(ScheduleEntry.COL_LESSON_COUNT, divs.size());
            scheduleValue.put(ScheduleEntry.COL_EVENTS, new String(events));

            scheduleValues.add(scheduleValue);
        }
        mContext.getContentResolver().bulkInsert(
                ScheduleEntry.CONTENT_URI,
                scheduleValues.toArray(new ContentValues[scheduleValues.size()]));

        mContext.getContentResolver().bulkInsert(
                LessonEntry.CONTENT_URI,
                lessonValues.toArray(new ContentValues[lessonValues.size()])
        );
        return FETCH_STATUS_SUCCESSFUL;
    }

    private
    @FetchStatus
    int fetchExams() {

        int classValue;
        BufferedReader reader = null;
        String html = "";

        final String CLASS_VALUE_PARAM = "cls";

        //Get the exams html from Blich's site
        try {
            classValue = getClassValue();
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

        } catch (BlichSyncAdapter.BlichFetchException e) {
            Log.e(LOG_TAG, "Error while trying to get user's class value", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
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
                monthDivider.put(ExamsEntry.COL_TEACHER, "wut");
                monthDivider.put(ExamsEntry.COL_DATE, dateInMillis);
                monthDivider.put(ExamsEntry.COL_SUBJECT, "" + currentMonth);

                contentValues.add(monthDivider);
            }

            mPreviousMonth = currentMonth;

            ContentValues row = new ContentValues();
            row.put(ExamsEntry.COL_DATE, dateInMillis);
            row.put(ExamsEntry.COL_SUBJECT, subject);
            row.put(ExamsEntry.COL_TEACHER, teachers);

            contentValues.add(row);
        }

        //Delete the whole exams table
        mContext.getContentResolver().delete(ExamsEntry.CONTENT_URI, null, null);
        //Repopulate the table with updated data
        mContext.getContentResolver().bulkInsert(
                ExamsEntry.CONTENT_URI,
                contentValues.toArray(new ContentValues[contentValues.size()]));

        return FETCH_STATUS_SUCCESSFUL;
    }

    private int getClassValue() throws BlichSyncAdapter.BlichFetchException {
        String currentClass = Utilities.Class.getCurrentClass(mContext);
        String selection;
        String[] selectionArgs;

        if (currentClass.contains("'")) { //Normal class syntax
            selection = ClassEntry.COL_GRADE + " = ? AND " +
                    ClassEntry.COL_GRADE_INDEX + " = ?";
            selectionArgs = currentClass.split("'");
        } else { //Abnormal class syntax
            selection = ClassEntry.COL_GRADE + " = ?";
            selectionArgs = new String[]{currentClass};
        }


        Cursor cursor = mContext.getContentResolver().query(
                ClassEntry.CONTENT_URI,
                new String[]{ClassEntry.COL_CLASS_INDEX},
                selection,
                selectionArgs,
                null);

        int classValue;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                classValue = cursor.getInt(0);
            } else {
                throw new BlichSyncAdapter.BlichFetchException("Can't get the user's class. " +
                        "Did the user configure his class?");
            }
        } else {
            throw new NullPointerException("Queried cursor is null");
        }

        cursor.close();
        return classValue;
    }

    private void addLessonToNotificationList(int classSettings,
                                             int day,
                                             int hour,
                                             String subject,
                                             String lessonType) {
        if (!lessonType.equals(LessonEntry.LESSON_TYPE_NORMAL)) {
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int tommorow = today + 1;
            if (tommorow == 8) tommorow = 1;

            int dayHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if ((today == day && dayHour < 17) || tommorow == day) {
                Lesson lesson = new Lesson(classSettings, day, hour, subject, lessonType);
                mLessonNotificationList.add(lesson);
            }
        }
    }

    private void notifyUser() {
        boolean foreground = Utilities.isAppOnForeground(getContext());

        if (!mLessonNotificationList.isEmpty() && !foreground) {
            Collections.sort(mLessonNotificationList);
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();

            int changesNum = 0;
            if (mLessonNotificationList.get(0).getDay() == today) {

                inboxStyle.addLine(buildTimetableBoldString(getContext().getResources().getString(
                        R.string.notification_update_today)));

                boolean tomorrow = false;
                for (Lesson lesson : mLessonNotificationList) {
                    if (lesson.getDay() == today) {
                        buildTimetableLine(inboxStyle, lesson);
                        changesNum++;
                    } else {
                        if (!tomorrow) {
                            inboxStyle.addLine(buildTimetableBoldString(getContext().getResources()
                                    .getString(R.string.notification_update_tomorrow)));
                        }
                        tomorrow = true;
                        buildTimetableLine(inboxStyle, lesson);
                        changesNum++;
                    }
                }
            } else {
                inboxStyle.addLine(buildTimetableBoldString(getContext().getResources().getString(
                        R.string.notification_update_tomorrow)));
                for (Lesson lesson : mLessonNotificationList) {
                    buildTimetableLine(inboxStyle, lesson);
                    changesNum++;
                }
            }

            String summery;
            if (changesNum == 1) summery = "ישנו שינוי אחד חדש";
            else summery = "ישנם " + changesNum + " שינויים חדשים";
            inboxStyle.setSummaryText(summery);

            int intKey = Preferences.PREF_NOTIFICATION_SOUND_KEY;
            String prefKey = Preferences.getKey(getContext(), intKey);
            String prefDefault = (String) Preferences.getDefault(getContext(), intKey);
            Uri ringtone = Uri.parse(Utilities
                    .getPrefString(getContext(),
                            prefKey,
                            prefDefault,
                            true));

            Intent intent = new Intent(getContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(
                    getContext(),
                    getContext().getString(R.string.notification_channel_schedule_id))
                    .setSmallIcon(R.drawable.ic_timetable_white_24dp)
                    .setContentTitle(getContext().getResources().getString(
                            R.string.notification_update_title))
                    .setContentText(summery)
                    .setSound(ringtone)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat.from(getContext())
                    .notify(NOTIFICATION_UPDATE_ID, notification);

        }
        mLessonNotificationList = new ArrayList<>();
    }

    private Spannable buildTimetableBoldString(String str) {
        Spannable sp = new SpannableString(str);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    private void buildTimetableLine(NotificationCompat.InboxStyle inboxStyle, Lesson lesson) {
        String str = "שעה " +
                lesson.getHour() +
                ": " +
                lesson.getSubject() +
                "\n";
        Spannable sp = new SpannableString(str);
        sp.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, str.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        inboxStyle.addLine(sp);
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    //A receiver that kicks off at 21:30 and 7:00
    public static class BlichUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Receiving Update Broadcast");
            Bundle bundle = new Bundle();
            bundle.putBoolean(SYNC_IS_PERIODIC, true);

            ContentResolver.requestSync(getSyncAccount(context),
                    context.getString(R.string.content_authority),
                    bundle);
        }
    }

    //A receiver that starts the Alarm Service when the device boots.
    public static class StartPeriodicSyncBootReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                initializeSyncAdapter(context);
            }
        }
    }

    public static class BlichFetchException extends Exception {
        public BlichFetchException(String message) {
            super(message);
        }
    }
}
