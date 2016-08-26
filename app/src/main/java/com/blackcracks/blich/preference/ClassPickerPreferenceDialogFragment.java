package com.blackcracks.blich.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.FetchClassService;
import com.blackcracks.blich.util.BlichDataUtils;
import com.blackcracks.blich.util.Utilities;

import java.util.Arrays;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class ClassPickerPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private ClassPickerPreference mPreference;
    private static final String[] sDisplayedValues = new String[]{"ט'", "י'", "יא'", "יב'"};
    private MaterialNumberPicker mGradeNumberPicker;
    private MaterialNumberPicker mGradePicker;
    private FrameLayout mProgressBar;
    private int mGradeIndex = 1;
    private int mGradeNumber = 1;

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
        setValue(mPreference.getValue());

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


    @Override
    protected void onBindDialogView(View view) {

        super.onBindDialogView(view);

        mGradeNumberPicker =
                (MaterialNumberPicker) view.findViewById(R.id.dialog_choose_class_number_picker);
        mGradePicker =
                (MaterialNumberPicker) view.findViewById(R.id.dialog_choose_class_name_picker);
        mGradePicker.setDisplayedValues(sDisplayedValues);

        mProgressBar = (FrameLayout) view.findViewById(R.id.picker_progressbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
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
    public void onDialogClosed(boolean isPositive) {
        if (isPositive) {
            mGradeIndex = mGradePicker.getValue();
            String currentGrade = sDisplayedValues[mGradeIndex - 1];
            mGradeNumber = mGradeNumberPicker.getValue();
            String grade = currentGrade + "/" + mGradeNumber;
            mPreference.setValue(grade);
        }
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
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getDialog().dismiss();
                            }
                        })
                .show();
    }

    private void setValue(String value) {
        String[] grade = value.split("/");
        String gradeString = grade[0];
        mGradeIndex = Arrays.asList(sDisplayedValues).indexOf(gradeString) + 1;
        mGradeNumber = Integer.parseInt(grade[1]);
    }

    private void setPickerValues(final int[] maxGradeNumber) {
        mProgressBar.setVisibility(View.GONE);
        mGradeNumberPicker.setVisibility(View.VISIBLE);
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        mGradeNumberPicker.setMaxValue(maxGradeNumber[mGradeIndex - 1]);
        mGradeNumberPicker.setValue(mGradeNumber);
        mGradePicker.setValue(mGradeIndex);
        mGradePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mGradeNumberPicker.setMaxValue(maxGradeNumber[newVal - 1]);
            }
        });
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
