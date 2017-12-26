/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.preference;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.TeacherFilterAdapter;
import com.blackcracks.blich.data.Lesson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class FilterPreferenceDialogFragment extends PreferenceDialogFragmentCompat
implements LoaderManager.LoaderCallbacks<List<Lesson>>{

    private Realm mRealm;

    private FilterPreference mPreference;
    private TeacherFilterAdapter mAdapter;

    public static final int TEACHER_LOADER_ID = 0;

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
        mRealm = Realm.getDefaultInstance();

        ListView listView = view.findViewById(R.id.list_view_teacher_filter);
        List<Lesson> lessons = new ArrayList<>();

        mPreference = (FilterPreference) getPreference();
        if (mPreference.getValue() != null && !mPreference.getValue().equals("")) {

            String persisted = mPreference.getValue();
            String[] subjectsAndTeachers = persisted.split(";");
            for (String subjectAndTeacher :
                    subjectsAndTeachers) {
                if (!subjectAndTeacher.equals("")) {
                    String[] arr = subjectAndTeacher.split(",");
                    String teacher = arr[0];
                    String subject = arr[1];

                    Lesson lesson = new Lesson();
                    lesson.setTeacher(teacher);
                    lesson.setSubject(subject);
                    lessons.add(lesson);
                }
            }
        }

        mAdapter = new TeacherFilterAdapter(getContext(), null, lessons);
        listView.setAdapter(mAdapter);

        getLoaderManager().initLoader(TEACHER_LOADER_ID, null, this);


        //Set up button click listeners
        Button selectAll = view.findViewById(R.id.btn_select_all);
        Button selectNone = view.findViewById(R.id.btn_select_none);

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.selectLessons(TeacherFilterAdapter.SELECT_ALL);
            }
        });


        selectNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.selectLessons(TeacherFilterAdapter.SELECT_NONE);
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            StringBuilder value = new StringBuilder();
            for (Lesson lesson :
                    mAdapter.getCheckLessons()) {
                String teacher = lesson.getTeacher();
                String subject = lesson.getSubject();

                value.append(teacher).append(",").append(subject).append(";");
            }
            mPreference.setValue(value.toString());
        }
        mRealm.close();
    }

    @Override
    public Loader<List<Lesson>> onCreateLoader(int id, Bundle args) {
        return new TeacherLoader(getContext(), mRealm);
    }

    @Override
    public void onLoadFinished(Loader<List<Lesson>> loader, List<Lesson> data) {
        mAdapter.switchData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Lesson>> loader) {
        mAdapter.switchData(null);
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
                    .notEqualTo("teacher", " ")
                    .findAll();

            List<Lesson> lessons = new ArrayList<>();
            for (Lesson lesson :
                    results) {
                if(!lesson.doesListContainLesson(lessons)) lessons.add(lesson);
            }

            Comparator<Lesson> compareBySubject = new Comparator<Lesson>() {
                @Override
                public int compare(Lesson o1, Lesson o2) {
                    return o1.getSubject().compareTo(o2.getSubject());
                }
            };
            Collections.sort(lessons, compareBySubject);

            deliverResult(lessons);
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }

}
