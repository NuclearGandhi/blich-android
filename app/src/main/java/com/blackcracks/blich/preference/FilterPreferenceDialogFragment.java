/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.preference;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.blackcracks.blich.data.BlichContract.LessonEntry;
import com.blackcracks.blich.data.BlichDatabaseHelper;

import java.util.ArrayList;

public class FilterPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private static final String[] PROJECTION = {
            LessonEntry.COL_TEACHER,
            LessonEntry.COL_SUBJECT
    };
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
        if (mPreference.getValue() != null) {
            String[] teachersAndSubjects = mPreference.getValue().split(";");
            for (String teacherAndSubject :
                    teachersAndSubjects) {
                String[] arr = teacherAndSubject.split(",");
                if (arr.length == 2) {
                    String teacher = arr[0];
                    String subject = arr[1];

                    mTeacherList.add(teacher);
                    mSubjectList.add(subject);
                }
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

        private ArrayList<String> mTeachers;
        private ArrayList<String> mSubjects;

        @Override
        protected Void doInBackground(Void... params) {

            BlichDatabaseHelper databaseHelper = new BlichDatabaseHelper(getContext());
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            String selection = LessonEntry.COL_TEACHER + " != ?";
            String[] selectionArs = {" "};
            String sortOrder = LessonEntry.COL_SUBJECT + " ASC";

            Cursor cursor = db.query(
                    true,
                    LessonEntry.TABLE_NAME,
                    PROJECTION,
                    selection,
                    selectionArs,
                    null, null,
                    sortOrder,
                    null
            );

            mTeachers = new ArrayList<>();
            mSubjects = new ArrayList<>();

            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    String teacher = cursor.getString(cursor.getColumnIndex(LessonEntry.COL_TEACHER));
                    String subject = cursor.getString(cursor.getColumnIndex(LessonEntry.COL_SUBJECT));
                    mTeachers.add(teacher);
                    mSubjects.add(subject);
                }
            }

            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            for (int i = 0; i < mTeachers.size(); i++) {
                final String teacher = mTeachers.get(i);
                final String subject = mSubjects.get(i);

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
