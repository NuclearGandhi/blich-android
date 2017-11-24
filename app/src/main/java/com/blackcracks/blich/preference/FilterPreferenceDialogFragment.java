/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.preference;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichDatabase;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import timber.log.Timber;

public class FilterPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private FilterPreference mPreference;
    private ViewGroup mTeacherScrollView;
    private ArrayList<String> mTeacherList;
    private ArrayList<String> mSubjectList;

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
        mPreference = (FilterPreference) getPreference();
        mTeacherScrollView = view.findViewById(R.id.filter_teacher_list);

        mTeacherList = new ArrayList<>();
        mSubjectList = new ArrayList<>();
        if (mPreference.getValue() != null && !mPreference.getValue().equals("")) {
            String[] teachersAndSubjects = mPreference.getValue().split(";");
            for (String teacherAndSubject :
                    teachersAndSubjects) {
                String[] arr = teacherAndSubject.split(",");
                String teacher = arr[0];
                String subject = arr[1];
                mTeacherList.add(teacher);
                mSubjectList.add(subject);
            }
        }

        new LoadTeacherList().execute();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = "";
            for (int i = 0; i < mTeacherList.size(); i++) {
                String teacher = mTeacherList.get(i);
                String subject = mSubjectList.get(i);
                value += teacher + "," + subject;
                if (i != mTeacherList.size() - 1)
                    value += ";";
            }
            mPreference.setValue(value);
        }
    }

    private class LoadTeacherList extends AsyncTask<Void, Void, Void> {

        private TreeMap<String, String> mTeachAndSub;

        @Override
        protected Void doInBackground(Void... params) {

            mTeachAndSub = new TreeMap<>();

            Query query = BlichDatabase.sDatabase.getView(BlichDatabase.TEACHER_VIEW_ID)
                    .createQuery();

            QueryEnumerator result;
            try {
                result = query.run();
                Iterator<QueryRow> it = result;
                while (it.hasNext()) {
                    QueryRow row = it.next();
                    String teacher = (String) row.getKey();
                    String subject = (String) row.getValue();

                    mTeachAndSub.put(teacher, subject);
                }
            } catch (CouchbaseLiteException e) {
                Timber.e(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);
            Set<Entry<String, String>> entries = mTeachAndSub.entrySet();
            List<Entry<String, String>> listOfEntries = new ArrayList<>(entries);

            Comparator<Entry<String, String>> valueComparator = new Comparator<Entry<String, String>>() {
                @Override
                public int compare(Entry<String, String> e1, Entry<String, String> e2) {
                    String v1 = e1.getValue();
                    String v2 = e2.getValue();
                    return v1.compareTo(v2);
                }
            };

            Collections.sort(listOfEntries, valueComparator);

            for (Entry<String, String> entry : listOfEntries) {
                final String teacher = entry.getKey();
                final String subject = entry.getValue();

                CheckBox view = (CheckBox) LayoutInflater.from(getContext())
                        .inflate(R.layout.teacher_filter_item, null);

                String formattedText = "<b>" + subject + "</b> - " + teacher;
                view.setText(Html.fromHtml(formattedText));

                for (int j = 0; j < mTeacherList.size(); j++) {
                    if (mTeacherList.get(j).equals(teacher) && mSubjectList.get(j).equals(subject)) {
                        view.setChecked(true);
                    }
                }
                view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mTeacherList.add(teacher);
                            mSubjectList.add(subject);
                        } else {
                            mTeacherList.remove(teacher);
                            mSubjectList.remove(subject);
                        }
                    }
                });
                mTeacherScrollView.addView(view);
            }
        }
    }
}
