/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.schedule.Lesson;

import java.lang.annotation.Retention;
import java.util.List;

import javax.annotation.Nullable;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * An {@link android.widget.Adapter} to display all the current teachers and their corresponding subject.
 */
public class TeacherFilterAdapter extends BaseAdapter {

    @Retention(SOURCE)
    @IntDef({SELECT_NONE, SELECT_ALL})
    private @interface SelectMode{}

    public static final int SELECT_ALL = 0;
    public static final int SELECT_NONE = 1;

    private Context mContext;
    private RealmTeacherHelper mRealmTeacherHelper;
    private List<Lesson> mFilteredTeachers;

    /**
     * @param data list of {@link Lesson}s to display
     * @param filteredTeachers list of previously chosen {@link Lesson}s.
     */
    @SuppressWarnings("SameParameterValue")
    public TeacherFilterAdapter(
            Context context,
            @Nullable List<Lesson> data,
            List<Lesson> filteredTeachers) {

        mContext = context;
        mRealmTeacherHelper = new RealmTeacherHelper(data);
        mFilteredTeachers = filteredTeachers;
    }

    @Override
    public int getCount() {
        return mRealmTeacherHelper.getLessonCount();
    }

    @Override
    public Object getItem(int position) {
        return mRealmTeacherHelper.getLesson(position);
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

        //Set the formatted text, subject in bold
        final Lesson lesson = (Lesson) getItem(position);
        String text = "<b>" + lesson.getSubject() + "</b> - " + lesson.getTeacher();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.checkBox.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.checkBox.setText(Html.fromHtml(text));
        }

        //Remove the listener, otherwise it will get called in the following command
        checkBox.setOnCheckedChangeListener(null);

        //Check the checkbox if it has been previously selected
        if (mFilteredTeachers.contains(lesson))
            checkBox.setChecked(true);
        else
            checkBox.setChecked(false);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If the box has been checked, add it to the checked teachers list
                if (isChecked && !mFilteredTeachers.contains(lesson))
                    mFilteredTeachers.add(lesson);
                else
                    mFilteredTeachers.remove(lesson);
            }
        });
    }

    /**
     * Select or deselect teachers in the list.
     *
     * @param select a {@link SelectMode}.
     */
    public void selectTeachers(@SelectMode int select) {
        switch(select) {
            case SELECT_ALL: {
                mFilteredTeachers.clear();
                mFilteredTeachers.addAll(mRealmTeacherHelper.getData());
                notifyDataSetChanged();
                break;
            }
            case SELECT_NONE: {
                mFilteredTeachers.clear();
                notifyDataSetChanged();
                break;
            }
            default:
                throw new IllegalArgumentException("Unrecognized select mode.");
        }
    }

    /**
     * @return all the selected {@link Lesson}s.
     */
    public List<Lesson> getFilteredTeachers() {
        return mFilteredTeachers;
    }

    static class ViewHolder {
        CheckBox checkBox;

        public ViewHolder(View view) {
            checkBox = (CheckBox) view;
        }
    }

    /**
     * A helper class to easily handle realm data requests.
     */
    static class RealmTeacherHelper {
        private List<Lesson> mData;
        private boolean mIsDataValid;

        /**
         * @param data data to instantiate the helper class with.
         */
        RealmTeacherHelper(List<Lesson> data) {
            switchData(data);
        }

        /**
         * @param data data to switch to. Checks validity of the data.
         */
        void switchData(List<Lesson> data) {
            mData = data;
            mIsDataValid = data != null && mData.size() != 0;
        }

        boolean isDataValid() {
            return mIsDataValid;
        }

        public List<Lesson> getData() {
            return mData;
        }

        /**
         * @param position position in the data list
         * @return {@link Lesson} in the specified position.
         */
        public Lesson getLesson(int position) {
            if (!mIsDataValid) return null;
            return mData.get(position);
        }

        /**
         * @return count of the number of {@link Lesson}s.
         */
        public int getLessonCount() {
            if (mIsDataValid) {
                return mData.size();
            } else {
                return 0;
            }
        }
    }
}
