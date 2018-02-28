/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.sync.BlichSyncUtils;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class SyncUtils {

    @Retention(SOURCE)
    @IntDef({FETCH_STATUS_SUCCESSFUL, FETCH_STATUS_UNSUCCESSFUL,
            FETCH_STATUS_NO_CONNECTION, FETCH_STATUS_CLASS_UNSUCCESSFUL})
    public @interface FetchStatus {
    }

    public static final int FETCH_STATUS_SUCCESSFUL = 0;
    public static final int FETCH_STATUS_UNSUCCESSFUL = 1;
    public static final int FETCH_STATUS_NO_CONNECTION = 2;

    public static final int FETCH_STATUS_CLASS_UNSUCCESSFUL = 101;

    public static void initializeSync(Context context) {
        BlichSyncUtils.initializeJobService(context);
        syncDatabase(context);
    }

    /**
     * Begin sync.
     */
    public static void syncDatabase(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        boolean isFetching = PreferencesUtils.getBoolean(
                context,
                Constants.Preferences.PREF_IS_SYNCING_KEY
        );
        if (!isFetching) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(context.getString(R.string.pref_is_syncing_key), true)
                    .apply();
            BlichSyncUtils.startImmediateSync(context);
        }
    }

    /**
     * Callback for when sync finished.
     *
     * @param status          a {@link FetchStatus} returned from
     *                        the sync.
     */
    public static void syncFinishedCallback(Context context,
                                            @FetchStatus int status,
                                            boolean dialogDismissible,
                                            final OnSyncRetryListener onRetryListener) {

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        @StringRes int titleString;
        @StringRes int messageString;
        switch (status) {
            case FETCH_STATUS_SUCCESSFUL: {
                return;
            }
            case FETCH_STATUS_NO_CONNECTION: {
                titleString = R.string.dialog_fetch_no_connection_title;
                messageString = R.string.dialog_fetch_no_connection_message;
                break;
            }
            case FETCH_STATUS_UNSUCCESSFUL: {
                titleString = R.string.dialog_fetch_unsuccessful_title;
                messageString = R.string.dialog_fetch_unsuccessful_message;
                break;
            }
            case FETCH_STATUS_CLASS_UNSUCCESSFUL: {
                titleString = R.string.dialog_fetch_unsuccessful_title;
                messageString = R.string.dialog_fetch_class_unsuccessful_message;
                break;
            }
            default: {
                titleString = R.string.dialog_fetch_unsuccessful_title;
                messageString = R.string.dialog_fetch_unsuccessful_message;
                break;
            }
        }

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(titleString)
                .content(messageString)
                .positiveText(R.string.dialog_try_again)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onRetryListener.onRetry();
                    }
                });

        if (dialogDismissible) {
            dialogBuilder.negativeText(R.string.dialog_cancel);
        } else {
            dialogBuilder.cancelable(false);
        }

        dialogBuilder.show();
    }

    public interface OnSyncRetryListener {
        void onRetry();
    }
}