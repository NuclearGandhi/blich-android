package com.blackcracks.blich.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.util.PreferenceUtils;

public class ExamReminderDialog extends DialogFragment {

    private static final String DIALOG_TAG_TIME_PICKER = "time_picker";

    private Button mTimeButton;

    private int mReminderHour;
    private int mReminderMinutes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReminderHour = PreferenceUtils.getInstance().getInt(R.string.pref_exam_reminder_hour);
        mReminderMinutes = PreferenceUtils.getInstance().getInt(R.string.pref_exam_reminder_minutes);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_exam_reminder, null, false);
        mTimeButton = rootView.findViewById(R.id.reminder_time);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog.Builder()
                        .setHour(mReminderHour)
                        .setMinutes(mReminderMinutes)
                        .setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(int hour, int minute) {
                                mReminderHour = hour;
                                mReminderMinutes = minute;
                                saveReminderTime();
                            }
                        })
                        .build()
                        .show(getFragmentManager(), DIALOG_TAG_TIME_PICKER);
            }
        });

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_exam_reminder_title);

        return builder.build();
    }

    private void saveReminderTime() {
        mTimeButton.setText(mReminderHour + ":" + mReminderMinutes);
        PreferenceUtils.getInstance().putInt(R.string.pref_exam_reminder_hour, mReminderHour);
        PreferenceUtils.getInstance().putInt(R.string.pref_exam_reminder_minutes, mReminderMinutes);
    }
}
