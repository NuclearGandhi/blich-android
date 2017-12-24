/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.preference;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.ListView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Lesson;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class FilterPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private FilterPreference mPreference;

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
        ListView listView = view.findViewById(R.id.list_view_teacher_filter);

        mPreference = (FilterPreference) getPreference();
        if (mPreference.getValue() != null && !mPreference.getValue().equals("")) {

        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = "";

            mPreference.setValue(value);
        }
    }

    private static class TeacherLoader extends Loader<List<Lesson>> {

        private Realm mRealm;

        public TeacherLoader(Context context, Realm realm) {
            super(context);
            mRealm = realm;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            RealmResults<Lesson> results = mRealm.where(Lesson.class)
                    .findAll();
            deliverResult(results);
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }

}
