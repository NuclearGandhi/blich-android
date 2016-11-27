package com.blackcracks.blich.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.activity.SettingsActivity;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.BlichDataUtils;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class BlichSyncAdapter extends AbstractThreadedSyncAdapter{

    public static final String ACTION_SYNC_FINISHED = "sync_finished";
    public static final String IS_SUCCESSFUL_EXTRA = "is_successful";

    private static final String LOG_TAG = BlichSyncAdapter.class.getSimpleName();

    private static final String SOURCE_URL =
            "http://blich.iscool.co.il/tabid/2117/language/he-IL/Default.aspx";

    private static final String VIEW_STATE = "__VIEWSTATE";
    private static final String LAST_FOCUS = "__LAS" +
            "TFOCUS";
    private static final String EVENT_ARGUMENT = "__EVENTARGUMENT";
    private static final String EVENT_TAGERT = "__EVENTTARGET";

    private static final String SELECOR_NAME = "dnn$ctr7919$TimeTableView$ClassesList";

    private static final String SCHEDULE_BUTTON_NAME = "dnn$ctr7919$TimeTableView$btnChangesTable";

    private static final String SCHEDULE_TABLE_ID = "dnn_ctr7919_TimeTableView_PlaceHolder";

    private static final String CELL_CLASS = "TTCell";
    private static final String CANCELED_LESSON_CLASS = "TableFreeChange";
    private static final String CHANGED_LESSON_CLASS = "TableFillChange";
    private static final String EXAM_LESSON_CLASS = "TableExamChange";
    private static final String EVENT_LESSON_CLASS = "TableEventChange";

    private static final int SYNC_INTERVAL = 180 * 60;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final int NOTIFICATION_UPDATE_ID = 100;

    private final Context mContext = getContext();
    private List<Lesson> mLessonNotificationList = new ArrayList<>();

    BlichSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    public static void initializeSyncAdapter(Context context) {
        Log.d(LOG_TAG, "Initializing sync");
        boolean isPeriodicSyncOn = Utilities.getPreferenceBoolean(context,
                SettingsActivity.SettingsFragment.PREF_NOTIFICATION_TOGGLE_KEY,
                SettingsActivity.SettingsFragment.PREF_NOTIFICATION_TOGGLE_DEFAULT);
        if (isPeriodicSyncOn) {
            configurePeriodicSync(context);
        } else {
            ContentResolver.removePeriodicSync(BlichSyncAdapter.getSyncAccount(context),
                    context.getString(R.string.content_authority),
                    Bundle.EMPTY);
        }
    }

    private static Account getSyncAccount(Context context) {

        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if ( null == accountManager.getPassword(newAccount) ) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
        }
        return newAccount;
    }

    private static void configurePeriodicSync(Context context) {
        Account account = getSyncAccount(context);
        ContentResolver.setSyncAutomatically(account,
                context.getString(R.string.content_authority), true);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME).
                    setSyncAdapter(account, authority).
                    setExtras(Bundle.EMPTY).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), SYNC_INTERVAL);
        }
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
        boolean manual = bundle.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);

        if (manual) {
            Log.d(LOG_TAG, "Syncing: Manual");
        } else {
            Log.d(LOG_TAG, "Syncing: Periodic");
        }

        boolean isSuccessful = fetchSchedule();

        Intent intent = new Intent(ACTION_SYNC_FINISHED);
        intent.putExtra(IS_SUCCESSFUL_EXTRA, isSuccessful);
        LocalBroadcastManager.getInstance(getContext())
                .sendBroadcast(intent);

        if (!manual) {
            notifyUser();
        }
    }

    private boolean fetchSchedule() {

        String currentClass = BlichDataUtils.ClassUtils.getCurrentClass(getContext());
        String selection;
        String[] selectionArgs;

        if (currentClass.contains("'")) {
            selectionArgs = currentClass.split("'");
            selection = BlichContract.ClassEntry.COL_GRADE + " = ? AND " +
                    BlichContract.ClassEntry.COL_GRADE_INDEX + " = ?";
        } else {
            selectionArgs = new String[]{currentClass};
            selection = BlichContract.ClassEntry.COL_GRADE + " = ?";
        }



        Cursor cursor = mContext.getContentResolver().query(
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
                return false;
            }
        } else {
            return false;
        }

        cursor.close();

        BufferedReader reader = null;
        String classHtml = "";

        try {
            /*
            get the html
             */
            URL viewStateUrl = new URL(SOURCE_URL);
            URLConnection viewStateCon = viewStateUrl.openConnection();
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
            String viewStateValue = viewState.attr("value");

            /*
            create a POST request
             */
            URL scheduleUrl = new URL(SOURCE_URL);
            HttpURLConnection scheduleCon = (HttpURLConnection) scheduleUrl.openConnection();
            scheduleCon.setDoOutput(true);

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair(EVENT_TAGERT, SCHEDULE_BUTTON_NAME));
            nameValuePairs.add(new BasicNameValuePair(EVENT_ARGUMENT, ""));
            nameValuePairs.add(new BasicNameValuePair(SELECOR_NAME, Integer.toString(classValue)));
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
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (classHtml.equals("")) {
            return false;
        }
        Document document = Jsoup.parse(classHtml);
        Elements lessons = document.getElementById(SCHEDULE_TABLE_ID).getElementsByClass(CELL_CLASS);

        List<ContentValues> values = new ArrayList<>();
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

            if (divs.size() != 0) {
                String[] subjects = new String[divs.size()];
                String[] classrooms = new String[divs.size()];
                String[] teachers = new String[divs.size()];
                String[] lessonTypes = new String[divs.size()];
                for (int k = 0; k < divs.size(); k++) {
                    Element div = divs.get(k);
                    String html = div.html();
                    String[] text = html.split("</b>");

                    subjects[k] = text[0].replace("<b>", "").replace("<br>", " ");
                    subjects[k] = Parser.unescapeEntities(subjects[k], false);

                    if (text.length == 2) {
                        text = text[1].split("<br>");

                        classrooms[k] = text[0].replace("&nbsp;&nbsp;", "").replace("(", "").replace(")", "");

                        teachers[k] = text[1];
                    } else {
                        classrooms[k] = " ";
                        teachers[k] = " ";
                    }

                    switch (div.attr("class")) {
                        case CANCELED_LESSON_CLASS: {
                            lessonTypes[k] = BlichContract.ScheduleEntry.LESSON_TYPE_CANCELED;
                            break;
                        }
                        case CHANGED_LESSON_CLASS: {
                            lessonTypes[k] = BlichContract.ScheduleEntry.LESSON_TYPE_CHANGED;
                            break;
                        }
                        case EXAM_LESSON_CLASS: {
                            lessonTypes[k] = BlichContract.ScheduleEntry.LESSON_TYPE_EXAM;
                            break;
                        }
                        case EVENT_LESSON_CLASS: {
                            lessonTypes[k] = BlichContract.ScheduleEntry.LESSON_TYPE_EVENT;
                            break;
                        }
                        default: {
                            lessonTypes[k] = BlichContract.ScheduleEntry.LESSON_TYPE_NORMAL;
                        }
                    }

                    addLessonToNotificationList(classValue,
                            column,
                            row,
                            subjects[k],
                            lessonTypes[k]);
                }

                String subjectsValue = subjects[0];
                for (int j = 1; j < subjects.length; j++) {
                    subjectsValue = subjectsValue + ";" + subjects[j];
                }
                String classroomsValue = classrooms[0];
                for (int j = 1; j < classrooms.length; j++) {
                    classroomsValue = classroomsValue + ";" + classrooms[j];
                }
                String teachersValue = teachers[0];
                for (int j = 1; j < teachers.length; j++) {
                    teachersValue = teachersValue + ";" + teachers[j];
                }
                String lessonTypesValue = lessonTypes[0];
                for (int j = 1; j < lessonTypes.length; j++) {
                    lessonTypesValue = lessonTypesValue + ";" + lessonTypes[j];
                }

                ContentValues value = new ContentValues();
                value.put(BlichContract.ScheduleEntry.COL_CLASS_SETTINGS, classValue);
                value.put(BlichContract.ScheduleEntry.COL_DAY, column);
                value.put(BlichContract.ScheduleEntry.COL_HOUR, row);
                value.put(BlichContract.ScheduleEntry.COL_SUBJECT, subjectsValue);
                value.put(BlichContract.ScheduleEntry.COL_CLASSROOM, classroomsValue);
                value.put(BlichContract.ScheduleEntry.COL_TEACHER, teachersValue);
                value.put(BlichContract.ScheduleEntry.COL_LESSON_TYPE, lessonTypesValue);

                values.add(value);
            }

        }
        mContext.getContentResolver().bulkInsert(
                BlichContract.ScheduleEntry.CONTENT_URI,
                values.toArray(new ContentValues[values.size()]));
        return true;
    }

    private void addLessonToNotificationList(int classSettings,
                                             int day,
                                             int hour,
                                             String subject,
                                             String lessonType) {
        if (!lessonType.equals(BlichContract.ScheduleEntry.LESSON_TYPE_NORMAL)) {
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

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        boolean foreground = Utilities.isAppOnForeground(getContext());

        if (!mLessonNotificationList.isEmpty() && !foreground &&
                !(8 < hour && hour < 17)) {
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
                        changesNum ++;
                    } else {
                        if (!tomorrow) {
                            inboxStyle.addLine(buildTimetableBoldString(getContext().getResources()
                                    .getString(R.string.notification_update_tomorrow)));
                        }
                        tomorrow = true;
                        buildTimetableLine(inboxStyle, lesson);
                        changesNum ++;
                    }
                }
            } else {
                inboxStyle.addLine(buildTimetableBoldString(getContext().getResources().getString(
                        R.string.notification_update_tomorrow)));
                for (Lesson lesson : mLessonNotificationList) {
                    buildTimetableLine(inboxStyle, lesson);
                    changesNum ++;
                }
            }

            String summery;
            if (changesNum == 1) summery = "ישנו שינוי אחד חדש";
            else summery = "ישנם " + changesNum + " שינויים חדשים";
            inboxStyle.setSummaryText(summery);

            Uri ringtone = Uri.parse(Utilities
                    .getPreferenceString(getContext(),
                            SettingsActivity.SettingsFragment.PREF_NOTIFICATION_SOUND_KEY,
                            SettingsActivity.SettingsFragment.PREF_NOTIFICATION_SOUND_DEFAULT,
                            true));

            Intent intent = new Intent(getContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(getContext())
                            .setSmallIcon(R.drawable.ic_timetable_white_24dp)
                            .setContentTitle(getContext().getResources().getString(
                            R.string.notification_update_title))
                            .setContentText(summery)
                            .setSound(ringtone)
                            .setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                            .setStyle(inboxStyle)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build();
            notification.defaults |= Notification.DEFAULT_VIBRATE;

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
}
