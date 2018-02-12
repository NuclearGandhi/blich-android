package com.blackcracks.blich.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.blackcracks.blich.R;

public class ClassPickerPreference extends DialogPreference {

    private int mValue;

    public ClassPickerPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setDialogLayoutResource(R.layout.dialog_select_class);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int defValue = (int) defaultValue;
        if (restorePersistedValue) {
            mValue = getPersistedInt(defValue);
        } else {
            mValue = defValue;
        }
    }

    public void setValue(int value) {
        mValue = value;
        persistInt(value);
    }

    public int getValue() {
        return mValue;
    }
}
