package com.blackcracks.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.BlichDataUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExamsFetchTest {

    public static final String LOG_TAG = ExamsFetchTest.class.getSimpleName();

    private static final String EXAMS_BASE_URL =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/MainHtmlExams.aspx?pid=17&mid=6264&layer=0";

    private static final String EXAMS_TABLE_ID = "ChangesList";

    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getContext();
    }

    @Test
    public void fetchExams() throws BlichSyncAdapter.BlichFetchException {

        //int classValue;
        int classValue = 15;
        BufferedReader reader = null;
        String html = "";

        final String CLASS_VALUE_PARAM = "cls";

        //Get the exams html from Blich's site
        try {
            //classValue = getClassValue();
            Uri baseUri = Uri.parse(EXAMS_BASE_URL).buildUpon()
                    .appendQueryParameter(CLASS_VALUE_PARAM, String.valueOf(classValue))
                    .build();

            URL url = new URL(baseUri.toString());
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);

            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            html = stringBuilder.toString();

        //} catch (BlichSyncAdapter.BlichFetchException e) {
        //    Log.e(LOG_TAG, "Error while trying to get user's class value", e);
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
            return;
            //return false;


        //Parse the html
        Document document = Jsoup.parse(html);
        Elements exams = document.getElementById(EXAMS_TABLE_ID).getElementsByTag("tr");

        List<ContentValues> contentValues = new ArrayList<>();

        //Parse the table rows in the html from the second row
        //(first row is the header of each column)
        for (int i = 1; i < exams.size(); i++) {
            Element exam = exams.get(i);
            Elements innerData = exam.getElementsByTag("td");
            String date = innerData.get(0).text();
            String subject = innerData.get(1).text();
            String teachers = innerData.get(2).text().replace(", ", ";");

            ContentValues row = new ContentValues();
            row.put(ExamsEntry.COL_DATE, date);
            row.put(ExamsEntry.COL_SUBJECT, subject);
            row.put(ExamsEntry.COL_TEACHER, teachers);

            contentValues.add(row);
        }

        mContext.getContentResolver().bulkInsert(
                ExamsEntry.CONTENT_URI,
                contentValues.toArray(new ContentValues[contentValues.size()]));

        Cursor cursor = mContext.getContentResolver().query(
                ExamsEntry.CONTENT_URI,
                null, null, null, null, null);

        Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(cursor));
        //return true;
    }

    private int getClassValue() throws BlichSyncAdapter.BlichFetchException {
        String currentClass = BlichDataUtils.ClassUtils.getCurrentClass(mContext);
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
                throw new BlichSyncAdapter.BlichFetchException("Can't get the user's class. Did the user configure his class?");
            }
        } else {
            throw new NullPointerException("Queried cursor is null");
        }

        cursor.close();
        return classValue;
    }

}
