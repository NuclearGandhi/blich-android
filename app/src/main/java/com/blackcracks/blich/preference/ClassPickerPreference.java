/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.PreferenceUtils;

/**
 * A preference to store the user's chosen {@link com.blackcracks.blich.data.ClassGroup}.
 */
public class ClassPickerPreference extends DialogPreference {

    private int mValue;

    public ClassPickerPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setDialogLayoutResource(R.layout.dialog_select_class);
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
