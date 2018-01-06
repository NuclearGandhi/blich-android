package com.blackcracks.blich.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichDatabaseHelper;
import com.blackcracks.blich.data.FetchClassService;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.Utilities;

import java.util.ArrayList;
import java.util.List;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * This DialogFragment is showed when the user launches the app for the first time to configure
 * some settings.
 * A similar dialog is shown when the user wants to change these settings:
 * {@link com.blackcracks.blich.preference.ClassPickerPreferenceDialogFragment}
 */
public class ChooseClassDialogFragment extends DialogFragment {

    public static final String PREF_IS_FIRST_LAUNCH_KEY = "first_launch";

    private AlertDialog mDialog;
    private MaterialNumberPicker mGradeNumberPicker;
    private MaterialNumberPicker mGradePicker;
    private FrameLayout mProgressBar;
    private BroadcastReceiver mFetchBroadcastReceiver;
    private OnDestroyListener mOnDestroyListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Create a {@link BroadcastReceiver} to listen when the data has finished downloading
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

    //Setup the Dialog
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
                rootView.findViewById(R.id.dialog_choose_class_number_picker);
        mGradePicker =
                rootView.findViewById(R.id.dialog_choose_class_name_picker);

        builder.setPositiveButton(R.string.dialog_okay,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] displayedValues = mGradePicker.getDisplayedValues();
                        String gradeName = displayedValues[mGradePicker.getValue()];
                        int gradeIndex = mGradeNumberPicker.getValue();
                        String grade;
                        if (gradeIndex == 0) {
                            grade = gradeName;
                        } else {
                            grade = gradeName + "'" + gradeIndex;
                        }
                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getContext());
                        sharedPreferences.edit()
                                .putString(Preferences.getKey(getContext(), Preferences.PREF_CLASS_PICKER_KEY),
                                        grade)
                                .apply();
                        sharedPreferences.edit()
                                .putBoolean(PREF_IS_FIRST_LAUNCH_KEY, false)
                                .apply();
                    }
                });

        mProgressBar = rootView.findViewById(R.id.picker_progressbar);
        mDialog = builder.create();
        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Disable the "okay" button
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        getClassData();
    }

    @Override
    public void onResume() {
        //Start the listener
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mFetchBroadcastReceiver,
                new IntentFilter(FetchClassService.ACTION_FINISHED_FETCH));
    }

    @Override
    public void onPause() {
        //Stop the listener
        super.onPause();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mFetchBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOnDestroyListener != null) mOnDestroyListener.onDestroy(getContext());
    }

    //If the fetching failed, show a Dialog
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
                .show();
    }

    //Start to fetch the data
    private void getClassData() {
        boolean isConnected = Utilities.isThereNetworkConnection(getContext());
        if (isConnected) {
            Intent intent = new Intent(getContext(), FetchClassService.class);
            getActivity().startService(intent);
        } else {
            onFetchFailed();
        }
    }

    public void setOnDestroyListener(OnDestroyListener listener) {
        mOnDestroyListener = listener;
    }

    //Get the downloaded data from the Class table
    private class GetGradesTask extends AsyncTask<Void, Void, Void> {

        List<String> mNormalGradesNamesArray = new ArrayList<>();
        List<String> mAbnormalGradesNamesArray = new ArrayList<>();
        List<Integer> mGradesIndexArray = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... voids) {
            BlichDatabaseHelper databaseHelper = new BlichDatabaseHelper(getContext());
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            Cursor cursor = db.query(
                    BlichContract.ClassEntry.TABLE_NAME,
                    new String[]{BlichContract.ClassEntry.COL_GRADE,
                            "MAX(" + BlichContract.ClassEntry.COL_GRADE_INDEX + ")"},
                    null,
                    null,
                    BlichContract.ClassEntry.COL_GRADE,
                    null, null);

            if (cursor.moveToFirst()) {
                int gradeNameCursorIndex = cursor.getColumnIndex(BlichContract.ClassEntry.COL_GRADE);
                int gradeIndexCursorIndex =
                        cursor.getColumnIndex("MAX(" + BlichContract.ClassEntry.COL_GRADE_INDEX + ")");

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
            onValueChangeListener.onValueChange(mGradePicker, 1, 1);

            mProgressBar.setVisibility(View.GONE);
            mGradePicker.setVisibility(View.VISIBLE);
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }

    public interface OnDestroyListener {
        void onDestroy(Context context);
    }
}
