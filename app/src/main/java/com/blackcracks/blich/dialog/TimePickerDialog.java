/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    protected Builder onCreateBuilder() {
        return new Builder();
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

        int timeHour = 7;
        int timeMinutes = 0;

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
