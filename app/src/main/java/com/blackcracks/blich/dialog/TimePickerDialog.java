package com.blackcracks.blich.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerDialog extends DialogFragment
        implements android.app.TimePickerDialog.OnTimeSetListener {

    private Builder mBuilder;
    private OnTimeSetListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null)
            throw new IllegalArgumentException("Dialog must be created using Builder");
        mBuilder = new Builder(args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new android.app.TimePickerDialog(getActivity(), this, mBuilder.timeHour, mBuilder.timeMinutes,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (mListener != null)
            mListener.onTimeSet(hourOfDay, minute);
    }

    public void setOnTimeSetListener(OnTimeSetListener listener) {
        mListener = listener;
    }

    public interface OnTimeSetListener {
        void onTimeSet(int hour, int minute);
    }

    public static class Builder {

        static final String KEY_TIME_HOUR = "time_hour";
        static final String KEY_TIME_MINUTES = "time_mintues";

        int timeHour;
        int timeMinutes;

        private OnTimeSetListener mListener;

        public Builder() {}

        private Builder(Bundle args) {
            timeHour = args.getInt(KEY_TIME_HOUR);
            timeMinutes = args.getInt(KEY_TIME_MINUTES);
        }

        public Builder setHour(int hour) {
            timeHour = hour;
            return this;
        }

        public Builder setMinutes(int minutes) {
            timeMinutes = minutes;
            return this;
        }

        public Builder setOnTimeSetListener(OnTimeSetListener listener) {
            mListener = listener;
            return this;
        }

        public TimePickerDialog build() {
            Bundle args = new Bundle();
            args.putInt(KEY_TIME_HOUR, timeHour);
            args.putInt(KEY_TIME_MINUTES, timeMinutes);

            TimePickerDialog dialog = new TimePickerDialog();
            dialog.setArguments(args);
            dialog.setOnTimeSetListener(mListener);
            return dialog;
        }
    }
}
