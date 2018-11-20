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

package com.blackcracks.blich.preference;

import android.content.Context;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.prefs.supportv7.ATEDialogPreference;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.raw.ClassGroup;
import com.blackcracks.blich.util.PreferenceUtils;

/**
 * A preference to store the user's chosen {@link ClassGroup}.
 */
public class ClassPickerPreference extends ATEDialogPreference {

    private int mValue;

    public ClassPickerPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setDialogLayoutResource(R.layout.dialog_class_picker);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int defValue = (int) PreferenceUtils.getInstance(getContext()).getDefaultValue(R.string.pref_user_class_group_key);
        if (restorePersistedValue) {
            mValue = getPersistedInt(defValue);
        } else {
            mValue = defValue;
        }
    }

    /**
     * Set the preference's value.
     *
     * @param value the value to set, must be a {@link ClassGroup} id.
     */
    public void setValue(int value) {
        mValue = value;
        persistInt(value);
    }

    public int getValue() {
        return mValue;
    }
}
