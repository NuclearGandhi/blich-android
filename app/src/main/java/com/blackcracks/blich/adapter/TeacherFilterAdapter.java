/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.adapter;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.TeacherSubject;

import java.util.List;

import javax.annotation.Nullable;

public class TeacherFilterAdapter extends BaseAdapter {

    public static final int SELECT_ALL = 0;
    public static final int SELECT_NONE = 1;

    private Context mContext;
    private RealmTeacherHelper mRealmTeacherHelper;
    private List<TeacherSubject> mTeacherSubjects;

    public TeacherFilterAdapter(
            Context context,
            @Nullable List<TeacherSubject> data,
            List<TeacherSubject> teacherSubjects) {

        mContext = context;
        mRealmTeacherHelper = new RealmTeacherHelper(data);
        mTeacherSubjects = teacherSubjects;
    }

    @Override
    public int getCount() {
        return mRealmTeacherHelper.getLessonCount();
    }

    @Override
    public Object getItem(int position) {
        return mRealmTeacherHelper.getTeacherSubject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mRealmTeacherHelper.isDataValid()) return null;

        View view;
        if (convertView == null) {
            view = newView(parent);
        } else {
            view = convertView;
        }
        bindView(position, view);
        return view;
    }

    private View newView(ViewGroup parent) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_teacher_filter, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    private void bindView(int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        CheckBox checkBox = (CheckBox) view;

        final TeacherSubject teacherSubject = (TeacherSubject) getItem(position);
        String text = "<b>" + teacherSubject.getSubject() + "</b> - " + teacherSubject.getTeacher();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.checkBox.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.checkBox.setText(Html.fromHtml(text));
        }

        checkBox.setOnCheckedChangeListener(null);

        if (mTeacherSubjects.contains(teacherSubject)){
            checkBox.setChecked(true);
        }
        else checkBox.setChecked(false);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !mTeacherSubjects.contains(teacherSubject)) mTeacherSubjects.add(teacherSubject);
                else mTeacherSubjects.remove(teacherSubject);
            }
        });
    }

    public void switchData(List<TeacherSubject> data) {
        mRealmTeacherHelper.switchData(data);
        notifyDataSetChanged();
    }

    public void selectLessons(int select) {
        switch(select) {
            case SELECT_ALL: {
                mTeacherSubjects.clear();
                mTeacherSubjects.addAll(mRealmTeacherHelper.getData());
                notifyDataSetChanged();
                break;
            }
            case SELECT_NONE: {
                mTeacherSubjects.clear();
                notifyDataSetChanged();
                break;
            }
        }
    }

    public List<TeacherSubject> getTeacherSubjects() {
        return mTeacherSubjects;
    }

    static class ViewHolder {
        CheckBox checkBox;

        public ViewHolder(View view) {
            checkBox = (CheckBox) view;
        }
    }

    static class RealmTeacherHelper {
        private List<TeacherSubject> mData;
        private boolean mIsDataValid;

        RealmTeacherHelper(List<TeacherSubject> data) {
            switchData(data);
        }

        void switchData(List<TeacherSubject> data) {
            mData = data;
            mIsDataValid = data != null && mData.size() != 0;
        }

        boolean isDataValid() {
            return mIsDataValid;
        }

        public List<TeacherSubject> getData() {
            return mData;
        }

        public TeacherSubject getTeacherSubject(int position) {
            if (!mIsDataValid) return null;
            return mData.get(position);
        }

        public int getLessonCount() {
            if (mIsDataValid) {
                return mData.size();
            } else {
                return 0;
            }
        }
    }
}
