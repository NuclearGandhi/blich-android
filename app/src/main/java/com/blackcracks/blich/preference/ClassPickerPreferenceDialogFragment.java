package com.blackcracks.blich.preference;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.sync.FetchClassService;
import com.blackcracks.blich.util.ClassGroupUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.Utilities;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import io.realm.Realm;

public class ClassPickerPreferenceDialogFragment extends PreferenceDialogFragmentCompat{

    private static final String KEY_DATA_VALID = "data_valid";
    private static final int CLASS_LOADER_ID = 1;

    private String[] mDisplayedValues;

    private ClassPickerPreference mPreference;

    private Realm mRealm;
    private boolean mIsDataValid = false;

    private MaterialNumberPicker mClassIndexPicker;
    private MaterialNumberPicker mGradePicker;
    private FrameLayout mProgressBar;

    private BroadcastReceiver mFetchBroadcastReceiver;

    public static ClassPickerPreferenceDialogFragment newInstance(Preference preference) {
        ClassPickerPreferenceDialogFragment fragment = new ClassPickerPreferenceDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = (ClassPickerPreference) getPreference();
        mRealm = Realm.getDefaultInstance();

        mFetchBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isSuccessful =
                        intent.getBooleanExtra(FetchClassService.IS_SUCCESSFUL_EXTRA, false);
                if (isSuccessful) {
                    setDataValid();
                } else {
                    onFetchFailed();
                }
            }
        };

        if (savedInstanceState != null) {
            mIsDataValid = savedInstanceState.getBoolean(KEY_DATA_VALID);
        } else {
            syncData();
        }
    }

    @Override
    protected void onBindDialogView(View view) {

        super.onBindDialogView(view);

        mClassIndexPicker =
                view.findViewById(R.id.dialog_choose_class_number_picker);
        mGradePicker =
                view.findViewById(R.id.dialog_choose_class_name_picker);

        mProgressBar = view.findViewById(R.id.picker_progressbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsDataValid) {
            setDataValid();
        } else {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mFetchBroadcastReceiver,
                        new IntentFilter(FetchClassService.ACTION_FINISHED_FETCH));
    }

    @Override
    public void onPause() {
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
    public void onDialogClosed(boolean isPositive) {
        if (isPositive) {
            String[] displayedValues = mGradePicker.getDisplayedValues();
            String gradeName = displayedValues[mGradePicker.getValue()];
            int classNum = mClassIndexPicker.getValue();
            int id;
            if (classNum == 0) {
                id = RealmUtils.getId(mRealm, gradeName);
            } else {
                id = RealmUtils.getId(mRealm, gradeName, classNum);
            }
            mPreference.setValue(id);
        }

        mRealm.close();
    }

    private void onFetchFailed() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fetch_failed,
                null);

        TextView message = view.findViewById(R.id.dialog_message);
        message.setText(R.string.dialog_fetch_class_no_connection_message);
        new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(R.string.dialog_try_again,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                syncData();
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getDialog().dismiss();
                            }
                        })
                .show();
    }

    private void syncData() {
        boolean isConnected = Utilities.isThereNetworkConnection(getContext());
        if (isConnected) {
            Intent intent = new Intent(getContext(), FetchClassService.class);
            getContext().startService(intent);
        } else {
            onFetchFailed();
        }
    }

    private void setDataValid() {
        mIsDataValid = true;
        mProgressBar.setVisibility(View.GONE);
        mGradePicker.setVisibility(View.VISIBLE);
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

        mDisplayedValues = ClassGroupUtils.loadDataIntoPicker(
                mRealm,
                mGradePicker,
                mClassIndexPicker,
                mPreference.getValue());
    }
}