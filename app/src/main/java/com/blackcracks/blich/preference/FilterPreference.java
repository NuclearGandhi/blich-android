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

/**
 * A preference to store the user's chosen teachers to filter.
 */
public class FilterPreference extends DialogPreference {

    private String mValue;

    public FilterPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_teacher_filter);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        String value = (String) defaultValue;
        if (restorePersistedValue) {
            value = getPersistedString(value);
        }
        mValue = value;
    }

    /**
     * Set the preference's value.
     *
     * @param value the value to set.
     */
    public void setValue(String value) {
        mValue = value;
        persistString(value);
    }

    public String getValue() {
        return mValue;
    }
}
