/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.preference;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.LessonEntry;

public class FilterPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private static final String[] PROJECTION = {
            LessonEntry.COL_TEACHER,
            LessonEntry.COL_SUBJECT
    };

    private View mTeacherList;

    public static FilterPreferenceDialogFragment newInstance(Preference preference) {
        FilterPreferenceDialogFragment fragment = new FilterPreferenceDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mTeacherList = view.findViewById(R.id.filter_teacher_list);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
    }
}
