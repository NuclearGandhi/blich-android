package com.blackcracks.blich.data;

import android.content.ContentValues;
import android.content.Context;

import com.blackcracks.blich.data.BlichContract.ClassEntry;
import com.blackcracks.blich.util.BlichDataUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FetchClassData extends FetchBlichData<Void, Void> {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = FetchClassData.class.getSimpleName();

    private static final String SOURCE_URL =
            "http://blich.iscool.co.il/tabid/2117/language/he-IL/Default.aspx";

    private static final String SELECTOR_ID = "dnn_ctr7919_TimeTableView_ClassesList";

    private Context mContext;

    public FetchClassData(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
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
            classHtml = builder.toString();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (classHtml.equals("")) {
            return false;
        }

        Document document = Jsoup.parse(classHtml);
        Element selector = document.getElementById(SELECTOR_ID);
        Elements options = selector.getElementsByTag("option");
        List<ContentValues> classValues = new ArrayList<>();
        int[] maxNumber = new int[4];
        for (Element option : options) {
            int class_index = Integer.parseInt(option.attr("value"));
            String className = option.text();
            String[] classNameSeparated = className.split(" - ");
            String grade = classNameSeparated[0];
            int grade_index = Integer.parseInt(classNameSeparated[1]);

            switch (grade) {
                case "ט":
                    maxNumber[0] = grade_index;
                    break;
                case "י":
                    maxNumber[1] = grade_index;
                    break;
                case "יא":
                    maxNumber[2] = grade_index;
                    break;
                case "יב":
                    maxNumber[3] = grade_index;
                    break;
            }

            ContentValues classValue = new ContentValues();
            classValue.put(ClassEntry.COL_CLASS_INDEX, class_index);
            classValue.put(ClassEntry.COL_GRADE, grade);
            classValue.put(ClassEntry.COL_GRADE_INDEX, grade_index);
            classValues.add(classValue);
        }
        BlichDataUtils.ClassUtils.setMaxGradeNumber(maxNumber);
        mContext.getContentResolver().bulkInsert(
                ClassEntry.CONTENT_URI, classValues.toArray(new ContentValues[classValues.size()]));
        return true;
    }
}
