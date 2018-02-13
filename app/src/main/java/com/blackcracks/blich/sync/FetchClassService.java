/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.blackcracks.blich.data.ClassGroup;
import com.blackcracks.blich.util.Constants.Database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * This class is used to fetch the current classes there are at Blich, from Blich's website
 */
public class FetchClassService extends IntentService {

    public static final String ACTION_FINISHED_FETCH = "finish_fetch";
    public static final String IS_SUCCESSFUL_EXTRA = "is_successful";

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
        URL url = BlichSyncUtils.buildURLFromUri(
                BlichSyncUtils.buildBaseUriFromCommand(BlichSyncUtils.COMMAND_CLASSES));

        RealmList<ClassGroup> data = new RealmList<>();

        try {
            String json = BlichSyncUtils.getResponseFromUrl(url);

            if (json.equals("")) return false;
            insertClassesJsonIntoData(json, data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        loadDataIntoRealm(data);
        return true;
    }

    private void insertClassesJsonIntoData(String json, List<ClassGroup> data) throws JSONException {
        JSONObject raw = new JSONObject(json);

        JSONArray classesJson = raw.getJSONArray(Database.JSON_ARRAY_CLASSES);

        for (int i = 0; i < classesJson.length(); i++) {
            JSONObject classJson = classesJson.getJSONObject(i);
            ClassGroup classGroup = new ClassGroup();

            classGroup.setId(classJson.getInt(Database.JSON_INT_ID));
            classGroup.setGrade(classJson.getInt(Database.JSON_INT_GRADE));
            classGroup.setName(classJson.getString(Database.JSON_STRING_NAME));
            classGroup.setNumber(classJson.getInt(Database.JSON_INT_NUMBER));

            data.add(classGroup);
        }
    }

    private void loadDataIntoRealm(RealmList<ClassGroup> data) {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        RealmResults<ClassGroup> classes = realm.where(ClassGroup.class)
                .findAll();
        classes.deleteAllFromRealm();

        realm.insert(data);
        realm.commitTransaction();
    }
}
