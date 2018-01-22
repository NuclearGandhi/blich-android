/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.blackcracks.blich.data.BlichContract;

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

/**
 * This class is used to fetch the current classes there are at Blich, from Blich's website
 */
public class FetchClassService extends IntentService {

    public static final String ACTION_FINISHED_FETCH = "finish_fetch";
    public static final String IS_SUCCESSFUL_EXTRA = "is_successful";

    private static final String SOURCE_URL =
            "http://blich.iscool.co.il/tabid/2117/language/he-IL/Default.aspx";
    private static final String SELECTOR_ID = "dnn_ctr7919_TimeTableView_ClassesList";

    public FetchClassService() {
        super("FetchClassService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isSuccessful = fetchClass();
        Intent broadcast = new Intent(ACTION_FINISHED_FETCH);
        broadcast.putExtra(IS_SUCCESSFUL_EXTRA, isSuccessful);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    private boolean fetchClass() {
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

        //Parse the html
        Document document = Jsoup.parse(classHtml);
        Element selector = document.getElementById(SELECTOR_ID);
        Elements options = selector.getElementsByTag("option");
        List<ContentValues> classValues = new ArrayList<>();
        for (Element option : options) {
            int class_index = Integer.parseInt(option.attr("value"));
            String className = option.text();
            //Insert the values to the Class Table.
            if (className.contains("-")) {
                String[] classNameSeparated = className.split(" - ");
                String grade = classNameSeparated[0];
                int grade_index = Integer.parseInt(classNameSeparated[1]);

                ContentValues classValue = new ContentValues();
                classValue.put(BlichContract.ClassEntry.COL_CLASS_INDEX, class_index);
                classValue.put(BlichContract.ClassEntry.COL_GRADE, grade);
                classValue.put(BlichContract.ClassEntry.COL_GRADE_INDEX, grade_index);
                classValues.add(classValue);
            } else {
                ContentValues classValue = new ContentValues();
                classValue.put(BlichContract.ClassEntry.COL_CLASS_INDEX, class_index);
                classValue.put(BlichContract.ClassEntry.COL_GRADE, className);
                classValues.add(classValue);
            }
        }
        getBaseContext().getContentResolver().bulkInsert(
                BlichContract.ClassEntry.CONTENT_URI,
                classValues.toArray(new ContentValues[classValues.size()]));
        return true;
    }
}
