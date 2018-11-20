/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.TeacherFilterAdapter;
import com.blackcracks.blich.data.TeacherSubject;
import com.blackcracks.blich.preference.FilterPreference;
import com.blackcracks.blich.util.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

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
        List<TeacherSubject> filteredTeachers = new ArrayList<>();

        //Get all the current filtered teachers
        String[] subjectsAndTeachers = PreferenceUtils.getInstance().getString(R.string.pref_filter_select_key).split(";");
        for (String subjectAndTeacher :
                subjectsAndTeachers) {
            if (!subjectAndTeacher.equals("")) {
                String[] arr = subjectAndTeacher.split(",");
                String teacher = arr[0];
                String subject = arr[1];

                TeacherSubject TeacherSubject = new TeacherSubject(teacher, subject);
                filteredTeachers.add(TeacherSubject);
            }
        }

        //Setup adapter
        mAdapter = new TeacherFilterAdapter(
                getContext(),
                fetchTeacherList(),
                filteredTeachers);
        listView.setAdapter(mAdapter);

        //Set up button click listeners
        ImageButton selectAll = view.findViewById(R.id.btn_select_all);
        ImageButton selectNone = view.findViewById(R.id.btn_select_none);

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
        for (TeacherSubject TeacherSubject :
                mAdapter.getFilteredTeachers()) {
            String teacher = TeacherSubject.getTeacher();
            String subject = TeacherSubject.getSubject();

            value.append(teacher).append(",").append(subject).append(";");
        }

        if (which == DialogAction.POSITIVE &&
                mPositiveClickListener != null)
            mPositiveClickListener.onPositiveClick(value.toString());
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
        return mRealm.where(TeacherSubject.class)
                .sort("subject")
                .findAll();
    }
}
