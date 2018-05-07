package com.blackcracks.blich.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerDialog extends BaseDialog<TimePickerDialog.Builder>
        implements android.app.TimePickerDialog.OnTimeSetListener {

    private OnTimeSetListener mListener;

    @Override
    protected void onCreateBuilder() {
        mBuilder = new Builder();
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

    public static class Builder extends BaseDialog.Builder {

        static final String KEY_TIME_HOUR = "time_hour";
        static final String KEY_TIME_MINUTES = "time_mintues";

        int timeHour;
        int timeMinutes;

        private OnTimeSetListener mListener;

        public Builder() {}

        @Override
        protected void setArgs(Bundle args) {
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

        @Override
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
