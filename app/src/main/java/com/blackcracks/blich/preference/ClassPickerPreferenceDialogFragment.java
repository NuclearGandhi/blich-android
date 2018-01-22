package com.blackcracks.blich.preference;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
import com.blackcracks.blich.data.BlichContract.ClassEntry;
import com.blackcracks.blich.data.BlichDatabaseHelper;
import com.blackcracks.blich.sync.FetchClassService;
import com.blackcracks.blich.util.Utilities;

import java.util.ArrayList;
import java.util.List;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class ClassPickerPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private ClassPickerPreference mPreference;

    private MaterialNumberPicker mGradeNumberPicker;
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

        mFetchBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isSuccessful =
                        intent.getBooleanExtra(FetchClassService.IS_SUCCESSFUL_EXTRA, false);
                if (isSuccessful) {
                    new GetGradesTask().execute();
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
                view.findViewById(R.id.dialog_choose_class_number_picker);
        mGradePicker =
                view.findViewById(R.id.dialog_choose_class_name_picker);

        mProgressBar = view.findViewById(R.id.picker_progressbar);
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
            String[] displayedValues = mGradePicker.getDisplayedValues();
            String gradeName = displayedValues[mGradePicker.getValue()];
            int gradeIndex = mGradeNumberPicker.getValue();
            if (gradeIndex == 0) {
                mPreference.setValue(gradeName);
            } else {
                mPreference.setValue(gradeName + "'" + gradeIndex);
            }
        }
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

    private void getClassData() {
        boolean isConnected = Utilities.isThereNetworkConnection(getContext());
        if (isConnected) {
            Intent intent = new Intent(getContext(), FetchClassService.class);
            getActivity().startService(intent);
        } else {
            onFetchFailed();
        }
    }

    private class GetGradesTask extends AsyncTask<Void, Void, Void> {

        List<String> mNormalGradesNamesArray = new ArrayList<>();
        List<String> mAbnormalGradesNamesArray = new ArrayList<>();
        List<Integer> mGradesIndexArray = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... voids) {
            BlichDatabaseHelper databaseHelper = new BlichDatabaseHelper(getContext());
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            Cursor cursor = db.query(
                    ClassEntry.TABLE_NAME,
                    new String[]{ClassEntry.COL_GRADE,
                            "MAX(" + ClassEntry.COL_GRADE_INDEX + ")"},
                    null,
                    null,
                    ClassEntry.COL_GRADE,
                    null, null);

            if (cursor.moveToFirst()) {
                int gradeNameCursorIndex = cursor.getColumnIndex(ClassEntry.COL_GRADE);
                int gradeIndexCursorIndex =
                        cursor.getColumnIndex("MAX(" + ClassEntry.COL_GRADE_INDEX + ")");

                mNormalGradesNamesArray = new ArrayList<>();
                mAbnormalGradesNamesArray = new ArrayList<>();
                mGradesIndexArray = new ArrayList<>();

                if (cursor.getInt(gradeIndexCursorIndex) != 0) {
                    mGradesIndexArray.add(cursor.getInt(gradeIndexCursorIndex));
                    mNormalGradesNamesArray.add(cursor.getString(gradeNameCursorIndex));
                } else {
                    mAbnormalGradesNamesArray.add(cursor.getString(gradeNameCursorIndex));
                }
                while (cursor.moveToNext()) {
                    if (cursor.getInt(gradeIndexCursorIndex) != 0) {
                        mGradesIndexArray.add(cursor.getInt(gradeIndexCursorIndex));
                        mNormalGradesNamesArray.add(cursor.getString(gradeNameCursorIndex));
                    } else {
                        mAbnormalGradesNamesArray.add(cursor.getString(gradeNameCursorIndex));
                    }
                }
            }

            cursor.close();
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {


            final List<String> displayedValues = mNormalGradesNamesArray;
            displayedValues.addAll(mAbnormalGradesNamesArray);

            int displayedValuesSize = displayedValues.size();

            mGradePicker.setMaxValue(displayedValuesSize - 1);
            mGradePicker.setDisplayedValues(displayedValues.toArray(new String[displayedValuesSize]));
            NumberPicker.OnValueChangeListener onValueChangeListener =
                    new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                    if (newVal > mGradesIndexArray.size() - 1) {
                        mGradeNumberPicker.setVisibility(View.INVISIBLE);
                        mGradeNumberPicker.setMinValue(0);
                        mGradeNumberPicker.setValue(0);
                    } else {
                        mGradeNumberPicker.setVisibility(View.VISIBLE);
                        mGradeNumberPicker.setMinValue(1);
                        mGradeNumberPicker.setMaxValue(mGradesIndexArray.get(newVal));
                    }
                }
            };
            mGradePicker.setOnValueChangedListener(onValueChangeListener);

            if (mPreference.getValue().contains("'")) {
                String[] currentValue = mPreference.getValue().split("'");
                int gradeNamePosition = displayedValues.indexOf(currentValue[0]);
                int gradeNumber = Integer.parseInt(currentValue[1]);
                onValueChangeListener.onValueChange(mGradePicker, 1, gradeNamePosition);
                mGradePicker.setValue(gradeNamePosition);
                mGradeNumberPicker.setValue(gradeNumber);
                mGradeNumberPicker.setVisibility(View.VISIBLE);
            } else {
                int gradePosition = displayedValues.indexOf(mPreference.getValue());
                mGradePicker.setValue(gradePosition);
                mGradeNumberPicker.setMinValue(0);
                mGradeNumberPicker.setValue(0);
                mGradeNumberPicker.setVisibility(View.INVISIBLE);
            }

            mProgressBar.setVisibility(View.GONE);
            mGradePicker.setVisibility(View.VISIBLE);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }
}
