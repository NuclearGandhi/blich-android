package com.blackcracks.blich.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.BlichContract.ClassEntry;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.Schedule;
import com.blackcracks.blich.util.Constants.Database;
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
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import timber.log.Timber;

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

    private static final long SECONDS_PER_MINUTE = 60;
    private static final long MINUTES_PER_HOUR = 60;
    private static final long SYNC_HOURS = 3;
    private static final long SYNC_INTERVAL = SYNC_HOURS * SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

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
    private List<Hour> mHourNotificationList = new ArrayList<>();

    BlichSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    public static void initializeSyncAdapter(Context context) {

        int intKey = Preferences.PREF_NOTIFICATION_TOGGLE_KEY;
        String prefKey = Preferences.getKey(context, intKey);
        boolean prefDefault = (boolean) Preferences.getDefault(context, intKey);
        boolean isPeriodicSyncOn = Utilities.getPrefBoolean(context,
                prefKey,
                prefDefault);

        Account syncAccount = getSyncAccount(context);
        String contentAuthority = context.getString(R.string.content_authority);

        if (isPeriodicSyncOn) {
            ContentResolver.addPeriodicSync(
                    syncAccount,
                    contentAuthority,
                    null,
                    SYNC_INTERVAL
            );
        } else {
            ContentResolver.removePeriodicSync(
                    syncAccount,
                    contentAuthority,
                    null
            );
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
            Timber.d("Syncing: Periodic");
        } else {
            Timber.d("Syncing: Manual");
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

    private @FetchStatus
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
            Timber.e(e, e.getMessage());

        } catch (BlichFetchException e) { //The user's class isn't configured properly
            //Let the user choose his class again
            return FETCH_STATUS_CLASS_NOT_CONFIGURED;

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                }
            }
        }

        //Handle not expected situations
        if (classHtml.equals("")) {
            return FETCH_STATUS_EMPTY_HTML;
        }
        Document document = Jsoup.parse(classHtml);
        Element table = document.getElementById(SCHEDULE_TABLE_ID);
        if (table == null) {
            return FETCH_STATUS_EMPTY_HTML;
        }
        Elements lessons = table.getElementsByClass(CELL_CLASS);


        //Initialize realm objects
        Schedule schedule = new Schedule();
        schedule.setClassId(classValue);
        RealmList<Hour> hourList = new RealmList<>();

        //Iterate through each cell in the table
        for (int i = 6; i < lessons.size(); i++) {
            int row = i / 6;
            int column = i % 6 + 1;

            Element lessonElement = lessons.get(i);
            Elements divs = lessonElement.getElementsByTag("div");

            Elements trs = lessonElement.getElementsByTag("tr");
            Elements tds = new Elements();
            for (Element tr : trs) {
                tds.add(tr.getElementsByTag("td").get(0));
            }

            divs.addAll(tds);

            Hour hour = new Hour();
            hour.setDay(column);
            hour.setHour(row);
            RealmList<Lesson> lessonList = new RealmList<>();

            for (int k = 0; k < divs.size(); k++) {
                Element div = divs.get(k);
                String html = div.html();
                String[] text = html.split("</b>");

                String subject, room, teacher, lessonType;

                subject = text[0].replace("<b>", "").replace("<br>", " ");
                subject = Parser.unescapeEntities(subject, false);
                subject = subject.trim();

                //TODO Improve this shitty code
                if (text.length == 2) {
                    text = text[1].split("<br>");

                    if (text.length == 2) {
                        room = text[0].replace("&nbsp;&nbsp;", "").replace("(", "").replace(")", "");
                        teacher = text[1].trim();
                    } else {
                        room = " ";
                        teacher = " ";
                    }
                } else {
                    room = " ";
                    teacher = " ";
                }

                switch (div.attr("class")) {
                    case CANCELED_LESSON_CLASS: {
                        lessonType = Database.TYPE_CANCELED;
                        break;
                    }
                    case CHANGED_LESSON_CLASS: {
                        lessonType = Database.TYPE_NEW_TEACHER;
                        break;
                    }
                    case EXAM_LESSON_CLASS: {
                        lessonType = Database.TYPE_EXAM;
                        break;
                    }
                    case EVENT_LESSON_CLASS: {
                        lessonType = Database.TYPE_EVENT;
                        break;
                    }
                    default: {
                        lessonType = Database.TYPE_NORMAL;
                    }
                }

                Lesson lesson = new Lesson(subject, room, teacher, lessonType);
                lessonList.add(lesson);
            }

            if (lessonList.size() != 0) {
                hour.setLessons(lessonList);
                hourList.add(hour);

                if (canAddToNotificationList(hour))
                    mHourNotificationList.add(hour);
            }
        }

        schedule.setSchedule(hourList);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.insert(schedule);
        realm.commitTransaction();
        realm.close();

        return FETCH_STATUS_SUCCESSFUL;
    }

    private @FetchStatus
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

    private boolean canAddToNotificationList(Hour hour) {

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int tomorrow = today + 1;
        if (tomorrow == 8) tomorrow = 1;

        int hourDay = hour.getDay();

        if (hourDay == today || hourDay == tomorrow) {
            List<Lesson> lessons = hour.getLessons();
            for (Lesson lesson:
                    lessons) {
                if (lesson.getChangeType().equals(Database.TYPE_NORMAL))
                    return true;
            }
        }
        return false;
    }

    private void notifyUser() {
        boolean foreground = Utilities.isAppOnForeground(getContext());

        if (!mHourNotificationList.isEmpty() && !foreground) {

            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();

            List<Lesson> todayNotificationLessons = new ArrayList<>();
            List<Lesson> tomorrowNotificationLessons = new ArrayList<>();
            for (Hour hour :
                    mHourNotificationList) {
                List<Lesson> lessons = hour.getLessons();
                for (Lesson lesson :
                        lessons) {
                    if (!lesson.getChangeType().equals(Database.TYPE_NORMAL)) {
                        if (hour.getDay() == day)
                            todayNotificationLessons.add(lesson);
                        else {
                            tomorrowNotificationLessons.add(lesson);
                        }
                    }
                }
            }

            if (todayNotificationLessons.size() != 0) {
                inboxStyle.addLine(getBoldText("היום:"));
                for (Lesson lesson :
                        todayNotificationLessons) {
                    inboxStyle.addLine(lesson.getSubject());
                }
            }


            if (tomorrowNotificationLessons.size() != 0) {
                inboxStyle.addLine(getBoldText("מחר:"));
                for (Lesson lesson :
                        tomorrowNotificationLessons) {
                    inboxStyle.addLine(lesson.getSubject());
                }
            }

            int changesNum = todayNotificationLessons.size() + tomorrowNotificationLessons.size();

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

            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_UPDATE_ID, notification);

        }
        mHourNotificationList = new ArrayList<>();
    }

    private Spanned getBoldText(String text) {
        return Html.fromHtml("<b> " + text + "</b>");
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

    public static class BlichFetchException extends Exception {
        public BlichFetchException(String message) {
            super(message);
        }
    }
}
