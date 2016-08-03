package com.blackcracks.blich.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.blackcracks.blich.R;

public class ClassPickerPreference extends DialogPreference {

    private String mValue;

    public ClassPickerPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setDialogLayoutResource(R.layout.dialog_select_class);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mValue = getPersistedString((String) defaultValue);
        } else {
            mValue = (String) defaultValue;
        }
    }

    public void setValue(String value) {
        mValue = value;
        persistString(mValue);
    }

    public String getValue() {
        return mValue;
    }
}
