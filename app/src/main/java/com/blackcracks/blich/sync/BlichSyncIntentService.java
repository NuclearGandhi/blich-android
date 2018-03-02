/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.blackcracks.blich.util.SyncUtils;
import com.blackcracks.blich.util.Utilities;

/**
 * An {@link IntentService} to run {@link BlichSyncTask#syncBlich(Context)} in the background.
 */
public class BlichSyncIntentService extends IntentService {

    public static final String ACTION_SYNC_FINISHED_CALLBACK = "sync_callback";
    public static final String EXTRA_FETCH_STATUS = "extra_fetch_status";

    public BlichSyncIntentService() {
        super(BlichSyncIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Begin sync and send its results.
        int status;
        if (Utilities.isThereNetworkConnection(getApplicationContext())) {
            status = BlichSyncTask.syncBlich(getApplicationContext());
        } else {
            status = SyncUtils.FETCH_STATUS_NO_CONNECTION;
        }
        sendBroadcast(getApplicationContext(), status);
        Utilities.updateWidget(getApplicationContext());
    }

    /**
     * Send a broadcast to all listeners that the sync has finished.
     *
     * @param status a {@link SyncUtils.FetchStatus} returned the from sync.
     */
    private static void sendBroadcast(Context context, @SyncUtils.FetchStatus int status) {
        Intent intent = new Intent(ACTION_SYNC_FINISHED_CALLBACK);
        intent.putExtra(EXTRA_FETCH_STATUS, status);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }
}
