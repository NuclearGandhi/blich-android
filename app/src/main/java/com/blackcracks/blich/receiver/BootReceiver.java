package com.blackcracks.blich.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blackcracks.blich.sync.BlichSyncHelper;

public class BootReceiver extends BroadcastReceiver {

    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !intent.getAction().equals(ACTION_BOOT_COMPLETED))
            return;

        BlichSyncHelper.initializePeriodicSync(context);
    }
}
