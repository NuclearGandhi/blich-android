/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.raw.ClassGroup;
import com.blackcracks.blich.sync.SyncClassGroupsService;
import com.blackcracks.blich.util.ClassGroupUtils;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.SyncCallbackUtils;
import com.blackcracks.blich.util.ThemeUtils;
import com.blackcracks.blich.util.Utilities;

import java.util.List;

import io.realm.Realm;

/**
 * This {@link DialogFragment} is showed when the user launches the app for the first time to configure
 * some settings.
 *
 * @see ClassPickerDialog
 */
@SuppressWarnings("ConstantConditions")
public class ClassPickerDialog extends BaseDialog<ClassPickerDialog.Builder> {

    private static final String KEY_DATA_VALID = "data_valid";

    private Realm mRealm;
    private boolean mIsDataValid = false;
    private boolean mIsClassConfigured = false;
    private int mId = -1;

    private MaterialDialog mDialog;
    private NumberPicker mClassIndexPicker;
    private NumberPicker mGradePicker;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mFetchBroadcastReceiver;
    private OnPositiveClickListener mOnDestroyListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();

        final SyncCallbackUtils.OnSyncRetryListener onSyncRetryListener = new SyncCallbackUtils.OnSyncRetryListener() {
            @Override
            public void onRetry() {
                syncData();
            }
        };

        //Create a BroadcastReceiver to listen when the data has finished downloading
        mFetchBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(SyncClassGroupsService.FETCH_STATUS_EXTRA, SyncCallbackUtils.FETCH_STATUS_UNSUCCESSFUL);
                if (status == SyncCallbackUtils.FETCH_STATUS_SUCCESSFUL) setDataValid();
                SyncCallbackUtils.syncFinishedCallback(getActivity(), status, mBuilder.isDismissible, onSyncRetryListener);
            }
        };

        if (savedInstanceState != null) {
            mIsDataValid = savedInstanceState.getBoolean(KEY_DATA_VALID);
        } else {
            syncData();
        }
    }

    @Override
    protected Builder onCreateBuilder() {
        return new Builder();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") final View rootView = inflater.inflate(
                R.layout.dialog_class_picker,
                null);

        setCancelable(mBuilder.isDismissible);

        mGradePicker =
                rootView.findViewById(R.id.dialog_class_picker_grade);
        mClassIndexPicker =
                rootView.findViewById(R.id.dialog_class_picker_index);

        mGradePicker.setMinValue(0);
        mGradePicker.setMaxValue(1);
        mGradePicker.setWrapSelectorWheel(false);
        ThemeUtils.themeNumberPicker(mGradePicker);

        mClassIndexPicker.setMinValue(1);
        mClassIndexPicker.setMaxValue(1);
        mClassIndexPicker.setWrapSelectorWheel(true);
        ThemeUtils.themeNumberPicker(mClassIndexPicker);

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext());
        dialogBuilder.customView(rootView, false);
        dialogBuilder.title(R.string.dialog_class_picker_title);
        dialogBuilder.positiveText(R.string.dialog_okay);
        dialogBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String[] displayedValues = mGradePicker.getDisplayedValues();
                String gradeName = displayedValues[mGradePicker.getValue()];
                int classNum = mClassIndexPicker.getValue();
                int id;
                int grade;
                if (mClassIndexPicker.getVisibility() == View.INVISIBLE) {//If an abnormal class group
                    id = RealmUtils.getId(mRealm, gradeName);
                    grade = 0;
                } else {
                    id = RealmUtils.getId(mRealm, gradeName, classNum);
                    grade = RealmUtils.getGrade(mRealm, id).getGrade();
                }

                mId = id;
                Utilities.setClassGroupProperties(getContext(), id, grade);
                mIsClassConfigured = true;
            }
        });

        if (mBuilder.doDisplayNegativeButton) dialogBuilder.negativeText(R.string.dialog_cancel);

        mProgressBar = rootView.findViewById(R.id.loading);
        mDialog = dialogBuilder.build();
        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsDataValid) {
            setDataValid();
        } else {
            mDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
        }
    }

    @Override
    public void onResume() {
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
        if (mIsClassConfigured && mOnDestroyListener != null) {
            mOnDestroyListener.onDestroy(getContext(), mId);
        }
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
        mDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);

        loadDataIntoPicker(
                mRealm,
                PreferenceUtils.getInstance().getInt(R.string.pref_user_class_group_key));
    }

    /**
     * Insert data from {@link ClassGroup}s into {@link NumberPicker}s.
     *
     * @param realm a {@link Realm} instance.
     * @param currentUserClassGroupId the current user's {@link ClassGroup} id.
     */
    public void loadDataIntoPicker(
            Realm realm,
            int currentUserClassGroupId) {

        final int[] maxIndexes = ClassGroupUtils.fetchMaxIndices(realm);
        List<ClassGroup> abnormalClasses = ClassGroupUtils.fetchAbnormalClasses(realm);

        String[] displayedValues = new String[4 + abnormalClasses.size()];
        displayedValues[0] = "ט";
        displayedValues[1] = "י";
        displayedValues[2] = "יא";
        displayedValues[3] = "יב";
        for (int i = 0; i < abnormalClasses.size(); i++) {
            displayedValues[i + 4] = abnormalClasses.get(i).getName();
        }

        mGradePicker.setDisplayedValues(displayedValues);
        mGradePicker.setMaxValue(displayedValues.length - 1);

        //Load values when grade changes
        NumberPicker.OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal < 4) { //Normal grade
                    mClassIndexPicker.setVisibility(View.VISIBLE);
                    mClassIndexPicker.setMaxValue(maxIndexes[newVal]);
                } else { //Abnormal
                    mClassIndexPicker.setVisibility(View.INVISIBLE);
                }
            }
        };
        mGradePicker.setOnValueChangedListener(valueChangeListener);

        ClassGroup classGroup = RealmUtils.getGrade(realm, currentUserClassGroupId);

        //Set current value
        int classIndex = 1;
        int gradeIndex;
        if (classGroup == null) {
            gradeIndex = 0;
        } else if (classGroup.isNormal()) {
            int grade = classGroup.getGrade();
            classIndex = classGroup.getNumber();

            gradeIndex = grade - 9;
        } else {
            gradeIndex = 4;
        }
        mGradePicker.setValue(gradeIndex);
        valueChangeListener.onValueChange(mGradePicker, 1, gradeIndex);
        mClassIndexPicker.setValue(classIndex);
    }

    public void setOnPositiveClickListener(OnPositiveClickListener listener) {
        mOnDestroyListener = listener;
    }

    public interface OnPositiveClickListener {
        void onDestroy(Context context, int id);
    }

    public static class Builder extends BaseDialog.Builder {

        static final String KEY_DISMISSIBLE = "dismissible";
        static final String KEY_DISPLAY_NEGATIVE_BUTTON = "display_negative_button";

        boolean isDismissible = true;
        boolean doDisplayNegativeButton = true;

        public Builder() {
        }

        @Override
        protected void setArgs(Bundle args) {
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

        @Override
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
