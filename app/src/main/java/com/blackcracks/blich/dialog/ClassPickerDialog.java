/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.blackcracks.blich.R;
import com.blackcracks.blich.sync.SyncClassGroupsService;
import com.blackcracks.blich.util.ClassGroupUtils;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.SyncUtils;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import io.realm.Realm;

/**
 * This {@link DialogFragment} is showed when the user launches the app for the first time to configure
 * some settings.
 *
 * @see ClassPickerDialog
 */
@SuppressWarnings("ConstantConditions")
public class ClassPickerDialog extends DialogFragment {

    private static final String KEY_DATA_VALID = "data_valid";
    public static final String PREF_IS_FIRST_LAUNCH_KEY = "first_launch";

    private Builder mBuilder;

    private Realm mRealm;
    private boolean mIsDataValid = false;

    private AlertDialog mDialog;
    private MaterialNumberPicker mClassIndexPicker;
    private MaterialNumberPicker mGradePicker;
    private FrameLayout mProgressBar;
    private BroadcastReceiver mFetchBroadcastReceiver;
    private OnDestroyListener mOnDestroyListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args.isEmpty())
            throw new IllegalArgumentException("Dialog must be created using Builder");
        mBuilder = new Builder(args);

        mRealm = Realm.getDefaultInstance();

        final SyncUtils.OnSyncRetryListener onSyncRetryListener = new SyncUtils.OnSyncRetryListener() {
            @Override
            public void onRetry() {
                syncData();
            }
        };

        //Create a BroadcastReceiver to listen when the data has finished downloading
        mFetchBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(SyncClassGroupsService.FETCH_STATUS_EXTRA, SyncUtils.FETCH_STATUS_UNSUCCESSFUL);
                if (status == SyncUtils.FETCH_STATUS_SUCCESSFUL) setDataValid();
                SyncUtils.syncFinishedCallback(getContext(), status, mBuilder.isDismissible, onSyncRetryListener);
            }
        };

        if (savedInstanceState != null) {
            mIsDataValid = savedInstanceState.getBoolean(KEY_DATA_VALID);
        } else {
            syncData();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") final View rootView = inflater.inflate(
                R.layout.dialog_select_class,
                null);

        setCancelable(mBuilder.isDismissible);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);

        mClassIndexPicker =
                rootView.findViewById(R.id.dialog_choose_class_number_picker);
        mGradePicker =
                rootView.findViewById(R.id.dialog_choose_class_name_picker);

        builder.setPositiveButton(R.string.dialog_okay,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] displayedValues = mGradePicker.getDisplayedValues();
                        String gradeName = displayedValues[mGradePicker.getValue()];
                        int classNum = mClassIndexPicker.getValue();
                        int id;
                        if (mClassIndexPicker.getVisibility() == View.INVISIBLE) {//If an abnormal class group
                            id = RealmUtils.getId(mRealm, gradeName);
                        } else {
                            id = RealmUtils.getId(mRealm, gradeName, classNum);
                        }

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getContext());

                        sharedPreferences.edit()
                                .putInt(
                                        Preferences.getKey(getContext(), Preferences.PREF_USER_CLASS_GROUP_KEY),
                                        id)
                                .apply();

                        sharedPreferences.edit()
                                .putBoolean(PREF_IS_FIRST_LAUNCH_KEY, false)
                                .apply();

                        if (mOnDestroyListener != null) {
                            mOnDestroyListener.onDestroy(getContext());
                        }
                    }
                });

        if (mBuilder.doDisplayNegativeButton)
            builder.setNegativeButton(R.string.dialog_cancel, null);

        mProgressBar = rootView.findViewById(R.id.picker_progressbar);
        mDialog = builder.create();
        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsDataValid) {
            setDataValid();
        } else {
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        //Start the listener
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mFetchBroadcastReceiver,
                        new IntentFilter(SyncClassGroupsService.ACTION_FINISHED_CLASS_GROUP_SYNC));
    }

    @Override
    public void onPause() {
        //Stop the listener
        super.onPause();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mFetchBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_DATA_VALID, mIsDataValid);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    /**
     * Start to fetch the data.
     */
    private void syncData() {
        Intent intent = new Intent(getContext(), SyncClassGroupsService.class);
        getActivity().startService(intent);
    }

    /**
     * Do all the necessary actions when data is valid.
     */
    private void setDataValid() {

        mIsDataValid = true;
        mProgressBar.setVisibility(View.GONE);
        mGradePicker.setVisibility(View.VISIBLE);
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

        ClassGroupUtils.loadDataIntoPicker(
                mRealm,
                mGradePicker,
                mClassIndexPicker,
                ClassGroupUtils.getClassValue(getContext()));
    }

    public void setOnDestroyListener(OnDestroyListener listener) {
        mOnDestroyListener = listener;
    }

    public interface OnDestroyListener {
        void onDestroy(Context context);
    }

    public static class Builder {

        static final String KEY_DISMISSIBLE = "dismissible";
        static final String KEY_DISPLAY_NEGATIVE_BUTTON = "display_negative_button";

        boolean isDismissible = true;
        boolean doDisplayNegativeButton = true;

        public Builder() {
        }

        private Builder(Bundle args) {
            isDismissible = args.getBoolean(KEY_DISMISSIBLE);
            doDisplayNegativeButton = args.getBoolean(KEY_DISPLAY_NEGATIVE_BUTTON);
        }

        public Builder setDismissible(boolean dismissible) {
            isDismissible = dismissible;
            return this;
        }

        public Builder setDisplayNegativeButton(boolean display) {
            doDisplayNegativeButton = display;
            return this;
        }

        public ClassPickerDialog build() {
            Bundle args = new Bundle();
            args.putBoolean(KEY_DISMISSIBLE, isDismissible);
            args.putBoolean(KEY_DISPLAY_NEGATIVE_BUTTON, doDisplayNegativeButton);

            ClassPickerDialog fragment = new ClassPickerDialog();
            fragment.setArguments(args);
            return fragment;
        }
    }
}
