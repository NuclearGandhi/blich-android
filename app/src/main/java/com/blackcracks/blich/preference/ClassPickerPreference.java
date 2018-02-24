/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants.Preferences;

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
        int defValue = (int) Preferences.getDefault(getContext(), Preferences.PREF_USER_CLASS_GROUP_KEY);
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
