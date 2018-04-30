/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.TeacherFilterAdapter;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.TeacherSubject;
import com.blackcracks.blich.preference.FilterPreference;
import com.blackcracks.blich.util.PreferenceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A dialog for {@link FilterPreference}. Prompts the user with a teacher list to filter.
 */
public class TeacherFilterDialog extends DialogFragment
        implements MaterialDialog.SingleButtonCallback {

    private Realm mRealm;
    private OnDestroyListener mOnDestroyListener;
    private OnPositiveClickListener mPositiveClickListener;

    private TeacherFilterAdapter mAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(getContext()).inflate(
                R.layout.dialog_teacher_filter,
                null);

        mRealm = Realm.getDefaultInstance();

        ListView listView = view.findViewById(R.id.list_view_teacher_filter);
        List<TeacherSubject> teacherSubjects = new ArrayList<>();

        //Get all the current filtered teachers
        String[] subjectsAndTeachers = PreferenceUtils.getInstance().getString(R.string.pref_filter_select_key).split(";");
        for (String subjectAndTeacher :
                subjectsAndTeachers) {
            if (!subjectAndTeacher.equals("")) {
                String[] arr = subjectAndTeacher.split(",");
                String teacher = arr[0];
                String subject = arr[1];

                TeacherSubject teacherSubject = new TeacherSubject(teacher, subject);
                teacherSubjects.add(teacherSubject);
            }
        }

        //Setup adapter
        mAdapter = new TeacherFilterAdapter(
                getContext(),
                fetchTeacherList(),
                teacherSubjects);
        listView.setAdapter(mAdapter);

        //Set up button click listeners
        Button selectAll = view.findViewById(R.id.btn_select_all);
        Button selectNone = view.findViewById(R.id.btn_select_none);

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.selectTeachers(TeacherFilterAdapter.SELECT_ALL);
            }
        });


        selectNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.selectTeachers(TeacherFilterAdapter.SELECT_NONE);
            }
        });

        return new MaterialDialog.Builder(getContext())
                .customView(view, false)
                .positiveText(R.string.dialog_okay)
                .negativeText(R.string.dialog_cancel)
                .onPositive(this)
                .build();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        StringBuilder value = new StringBuilder();
        for (TeacherSubject teacherSubject :
                mAdapter.getTeacherSubjects()) {
            String teacher = teacherSubject.getTeacher();
            String subject = teacherSubject.getSubject();

            value.append(teacher).append(",").append(subject).append(";");
        }

        if (mPositiveClickListener != null) mPositiveClickListener.onPositiveClick(value.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOnDestroyListener != null) mOnDestroyListener.onDestroy();
        mRealm.close();
    }

    public void setOnDestroyListener(OnDestroyListener onDestroyListener) {
        mOnDestroyListener = onDestroyListener;
    }

    public void setOnPositiveClickListener(OnPositiveClickListener positiveClickListener) {
        mPositiveClickListener = positiveClickListener;
    }

    public interface OnDestroyListener {
        void onDestroy();
    }

    public interface OnPositiveClickListener {
        void onPositiveClick(String value);
    }

    private List<TeacherSubject> fetchTeacherList() {
        RealmResults<Lesson> results = mRealm.where(Lesson.class)
                .notEqualTo("teacher", " ")
                .findAll();

        List<TeacherSubject> teacherSubjects = new ArrayList<>();
        for (Lesson lesson :
                results) {
            TeacherSubject teacherSubject = lesson.getTeacherSubject();
            if (!teacherSubjects.contains(teacherSubject))
                teacherSubjects.add(teacherSubject);
        }

        Comparator<TeacherSubject> compareBySubject = new Comparator<TeacherSubject>() {
            @Override
            public int compare(TeacherSubject o1, TeacherSubject o2) {
                return o1.getSubject().compareTo(o2.getSubject());
            }
        };
        Collections.sort(teacherSubjects, compareBySubject);
        return teacherSubjects;
    }
}
