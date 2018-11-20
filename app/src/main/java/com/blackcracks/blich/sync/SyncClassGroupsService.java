/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.blackcracks.blich.data.raw.ClassGroup;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.ShahafUtils;
import com.blackcracks.blich.util.SyncCallbackUtils;
import com.blackcracks.blich.util.SyncCallbackUtils.FetchStatus;
import com.blackcracks.blich.util.Utilities;

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
 * An {@link IntentService} to fetch the current class groups that are at Blich.
 */
public class SyncClassGroupsService extends IntentService {

    public static final String ACTION_FINISHED_CLASS_GROUP_SYNC = "finished_fetch";
    public static final String FETCH_STATUS_EXTRA = "fetch_status";

    public SyncClassGroupsService() {
        super("SyncClassGroupsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        @FetchStatus int status = beginSync();
        Intent broadcast = new Intent(ACTION_FINISHED_CLASS_GROUP_SYNC);
        broadcast.putExtra(FETCH_STATUS_EXTRA, status);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    private @FetchStatus int beginSync() {
        if (Utilities.isThereNetworkConnection(getApplicationContext())) {
            return fetchClass();
        } else {
            return SyncCallbackUtils.FETCH_STATUS_CLASS_UNSUCCESSFUL;
        }
    }

    /**
     * Begin class group fetching.
     *
     * @return If the fetch is successful
     */
    private @FetchStatus int fetchClass() {
        URL url = ShahafUtils.buildURLFromUri(
                ShahafUtils.buildBaseUriFromCommand(ShahafUtils.COMMAND_CLASSES));

        RealmList<ClassGroup> data = new RealmList<>();

        try {
            String json = ShahafUtils.getResponseFromUrl(url);

            if (json == null || json.equals("")) return SyncCallbackUtils.FETCH_STATUS_CLASS_UNSUCCESSFUL;
            insertClassesJsonIntoData(json, data);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        loadDataIntoRealm(data);
        return SyncCallbackUtils.FETCH_STATUS_SUCCESSFUL;
    }

    /**
     * Insert data into a list.
     *
     * @param json JSON to parse.
     * @param data List to insert data into.
     */
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

    /**
     * Insert data in {@link Realm}.
     *
     * @param data Data to be inserted
     */
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
