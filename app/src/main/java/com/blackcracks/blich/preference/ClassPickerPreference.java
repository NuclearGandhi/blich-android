/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.preference;

import android.content.Context;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.prefs.supportv7.ATEDialogPreference;
import com.blackcracks.blich.R;
import com.blackcracks.blich.util.PreferenceUtils;

/**
 * A preference to store the user's chosen {@link com.blackcracks.blich.data.ClassGroup}.
 */
public class ClassPickerPreference extends ATEDialogPreference {

    private int mValue;

    public ClassPickerPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setDialogLayoutResource(R.layout.dialog_class_picker);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int defValue = (int) PreferenceUtils.getInstance().getDefaultValue(R.string.pref_user_class_group_key);
        if (restorePersistedValue) {
            mValue = getPersistedInt(defValue);
        } else {
            mValue = defValue;
        }
    }

    /**
     * Set the preference's value.
     *
     * @param value the value to set, must be a {@link com.blackcracks.blich.data.ClassGroup} id.
     */
    public void setValue(int value) {
        mValue = value;
        persistInt(value);
    }

    public int getValue() {
        return mValue;
    }
}
