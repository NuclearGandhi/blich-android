package com.blackcracks.blich.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.FetchClassService;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.BlichDataUtils;
import com.blackcracks.blich.util.Utilities;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;


public class ChooseClassDialogFragment extends DialogFragment {

    public static final String PREF_IS_FIRST_LAUNCH_KEY = "first_launch";

    private static final String[] sDisplayedValues = new String[]{"ט'", "י'", "יא'", "יב'"};

    private AlertDialog mDialog;
    private MaterialNumberPicker mGradeNumberPicker;
    private MaterialNumberPicker mGradePicker;
    private FrameLayout mProgressBar;
    private BroadcastReceiver mFetchBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFetchBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isSuccessful =
                        intent.getBooleanExtra(FetchClassService.IS_SUCCESSFUL_EXTRA, false);
                if (isSuccessful) {
                    setPickerValues(BlichDataUtils.ClassUtils.getMaxGradeNumber());
                } else {
                    onFetchFailed();
                }
            }
        };
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View rootView = inflater.inflate(
                R.layout.dialog_select_class,
                null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);

        mGradeNumberPicker =
                (MaterialNumberPicker) rootView.findViewById(R.id.dialog_choose_class_number_picker);
        mGradePicker =
                (MaterialNumberPicker) rootView.findViewById(R.id.dialog_choose_class_name_picker);
        mGradePicker.setDisplayedValues(sDisplayedValues);

        builder.setPositiveButton(R.string.dialog_okay,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int currentGradeIndex = mGradePicker.getValue();
                        String currentGrade = sDisplayedValues[currentGradeIndex - 1];
                        int currentGradeNumber = mGradeNumberPicker.getValue();
                        String grade = currentGrade + "/" + currentGradeNumber;
                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getContext());
                        sharedPreferences.edit()
                                .putString(SettingsFragment.PREF_CLASS_PICKER_KEY,
                                        grade)
                                .apply();
                        sharedPreferences.edit()
                                .putBoolean(PREF_IS_FIRST_LAUNCH_KEY, false)
                                .apply();
                    }
                });

        mProgressBar = (FrameLayout) rootView.findViewById(R.id.picker_progressbar);
        mDialog = builder.create();
        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        getClassData();
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
    public void onDestroy() {
        super.onDestroy();
        BlichSyncAdapter.initializeSyncAdapter(getContext());
    }

    private void setPickerValues(final int[] maxGradeNumber) {
        mProgressBar.setVisibility(View.GONE);
        mGradeNumberPicker.setVisibility(View.VISIBLE);
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        mGradeNumberPicker.setMaxValue(maxGradeNumber[0]);
        mGradePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mGradeNumberPicker.setMaxValue(maxGradeNumber[newVal - 1]);
            }
        });
    }

    private void onFetchFailed() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_no_connection,
                null);
        TextView message = (TextView) view.findViewById(R.id.dialog_message);
        message.setText(R.string.dialog_no_connection_message);
        new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(R.string.dialog_no_connection_try_again,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getClassData();
                            }
                        })
                .show();
    }

    private void getClassData() {
        boolean isConnected = Utilities.isThereNetworkConnection(getContext());
        if (isConnected) {
            Intent intent = new Intent(getContext(), FetchClassService.class);
            getActivity().startService(intent);
        } else {
            onFetchFailed();
        }
    }
}
