package com.blackcracks.blich.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.blackcracks.blich.data.BlichContract.ClassEntry;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.util.BlichDataUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.List;

/**
 * <h1>Fetch Schedule Data from Blich's Website</h1>
 * This class fetches the current schedule of a given class from blich's website by
 * using POST requests to get the html.
 * The html is then parsed and writes the the wanted values to a database.
 *
 * @author zeBlackCracks
 */

@SuppressWarnings("SpellCheckingInspection")
public class FetchScheduleData extends FetchBlichData<Void, Void> {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = FetchScheduleData.class.getSimpleName();

    private static final String SOURCE_URL =
            "http://blich.iscool.co.il/tabid/2117/language/he-IL/Default.aspx";

    private static final String VIEW_STATE = "__VIEWSTATE";
    private static final String LAST_FOCUS = "__LASTFOCUS";
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

    private Context mContext;

    public FetchScheduleData(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String[] classString = BlichDataUtils.ClassUtils.getCurrentClass(mContext)
                .split("\'");

        String grade = classString[0];
        String gradeNumber = classString[1];

        Cursor cursor = mContext.getContentResolver().query(
                BlichContract.ClassEntry.CONTENT_URI,
                new String[]{ClassEntry.COL_CLASS_INDEX},
                ClassEntry.COL_GRADE + " = ? AND " + ClassEntry.COL_GRADE_INDEX + " = ?",
                new String[]{grade, gradeNumber},
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

            if (divs.size() != 0) {
                String[] subjects = new String[divs.size()];
                String[] classrooms = new String[divs.size()];
                String[] teachers = new String[divs.size()];
                String[] lessonTypes = new String[divs.size()];
                for (int k = 0; k < divs.size(); k++) {
                    Element div = divs.get(k);
                    String html = div.html();
                    String[] text = html.split("</b>");

                    subjects[k] = text[0].replace("<b>", "");

                    if (!text[1].equals("")) {
                        text = text[1].split("<br>");

                        classrooms[k] = text[0].replace("&nbsp;&nbsp;", "").replace("(", "").replace(")", "");

                        teachers[k] = text[1];
                    }

                    switch (div.attr("class")) {
                        case CANCELED_LESSON_CLASS: {
                            lessonTypes[k] = ScheduleEntry.LESSON_TYPE_CANCELED;
                            break;
                        }
                        case CHANGED_LESSON_CLASS: {
                            lessonTypes[k] = ScheduleEntry.LESSON_TYPE_CHANGED;
                            break;
                        }
                        case EXAM_LESSON_CLASS: {
                            lessonTypes[k] = ScheduleEntry.LESSON_TYPE_EXAM;
                            break;
                        }
                        case EVENT_LESSON_CLASS: {
                            lessonTypes[k] = ScheduleEntry.LESSON_TYPE_EVENT;
                            break;
                        }
                        default: {
                            lessonTypes[k] = ScheduleEntry.LESSON_TYPE_NORMAL;
                        }
                    }
                }

                String subjectsValue = subjects[0];
                for (int j = 1; j < subjects.length; j++) {
                    subjectsValue = subjectsValue + "," + subjects[j];
                }
                String classroomsValue = classrooms[0];
                for (int j = 1; j < classrooms.length; j++) {
                    classroomsValue = classroomsValue + "," + classrooms[j];
                }
                String teachersValue = teachers[0];
                for (int j = 1; j < teachers.length; j++) {
                    teachersValue = teachersValue + "," + teachers[j];
                }
                String lessonTypesValue = lessonTypes[0];
                for (int j = 1; j < lessonTypes.length; j++) {
                    lessonTypesValue = lessonTypesValue + "," + lessonTypes[j];
                }

                ContentValues value = new ContentValues();
                value.put(ScheduleEntry.COL_CLASS_SETTINGS, classValue);
                value.put(ScheduleEntry.COL_DAY, column);
                value.put(ScheduleEntry.COL_HOUR, row);
                value.put(ScheduleEntry.COL_SUBJECT, subjectsValue);
                value.put(ScheduleEntry.COL_CLASSROOM, classroomsValue);
                value.put(ScheduleEntry.COL_TEACHER, teachersValue);
                value.put(ScheduleEntry.COL_LESSON_TYPE, lessonTypesValue);

                values.add(value);
            }

        }
        mContext.getContentResolver().bulkInsert(
                ScheduleEntry.CONTENT_URI,
                values.toArray(new ContentValues[values.size()]));
        return true;
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
