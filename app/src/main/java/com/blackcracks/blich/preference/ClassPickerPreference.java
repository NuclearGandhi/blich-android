package com.blackcracks.blich.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.FetchBlichData;
import com.blackcracks.blich.data.FetchClassData;
import com.blackcracks.blich.util.BlichDataUtils;

import java.util.Arrays;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class ClassPickerPreference extends DialogPreference implements FetchBlichData.OnFetchFinishListener {

    private static final String[] sDisplayedValues = new String[]{"ט'", "י'", "יא'", "יב'"};
    private MaterialNumberPicker mGradeNumberPicker;
    private MaterialNumberPicker mGradePicker;
    FrameLayout mProgressBar;
    private int mGradeIndex = 1;
    private int mGradeNumber = 1;

    public ClassPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_select_class);
    }

    @Override
    protected View onCreateView(final ViewGroup viewGroup) {
        View view = super.onCreateView(viewGroup);
        view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return view;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedString((String) defaultValue));
        } else {
            setValue((String) defaultValue);
        }
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
    protected void showDialog(Bundle savedInstanceState) {
        super.showDialog(savedInstanceState);
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        new FetchClassData(getContext()).addOnFetchFinishListener(this).execute();
    }

    @Override
    protected void onDialogClosed(boolean isPositive) {
        if (isPositive) {
            mGradeIndex = mGradePicker.getValue();
            String currentGrade = sDisplayedValues[mGradeIndex - 1];
            mGradeNumber = mGradeNumberPicker.getValue();
            String grade = currentGrade + "/" + mGradeNumber;
            persistString(grade);
        }
    }

    @Override
    public void onFetchFinished(boolean isSuccessful) {
        if (isSuccessful) {
            setPickerValues(BlichDataUtils.ClassUtils.getMaxGradeNumber());
        } else {
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
                            new FetchClassData(getContext())
                                    .addOnFetchFinishListener(ClassPickerPreference.this)
                                    .execute();
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
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
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
}
