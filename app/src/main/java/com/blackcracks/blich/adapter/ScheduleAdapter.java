package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;

public class ScheduleAdapter extends CursorTreeAdapter {

    private static final String TAG = ScheduleAdapter.class.getSimpleName();

    private static final String HOUR_KEY = "hour_key";

    private final Context mContext;
    private final ExpandableListView mListView;
    private final LoaderManager mLoaderManager;
    private final int mDay;

    private SparseBooleanArray mExpandedGroups = new SparseBooleanArray();

    public ScheduleAdapter(Cursor cursor,
                           @NonNull Context context,
                           @NonNull final ExpandableListView listView,
                           @NonNull LoaderManager loaderManager,
                           int day) {
        super(cursor, context);

        mContext = context;
        mListView = listView;
        mLoaderManager = loaderManager;
        mDay = day;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {

        if (getCursor() == null) {
            return null; //Don't run the following code if the cursor is null
        }

        int hour = groupCursor.getInt(groupCursor.getColumnIndex(ScheduleEntry.COL_HOUR));
        int groupPosition = groupCursor.getPosition();

        Bundle args = new Bundle();
        args.putInt(HOUR_KEY, hour);

        //Get the cursor containing the all lessons in the specific day and hour
        Loader<Cursor> loader = mLoaderManager.getLoader(groupPosition);
        if (loader != null && !loader.isReset()) {
            mLoaderManager.restartLoader(groupPosition, args, new ChildLoaderCallback());
        } else {
            mLoaderManager.initLoader(groupPosition, args, new ChildLoaderCallback());
        }
        return null;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.schedule_group, parent, false);

        GroupViewHolder holder = new GroupViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        final GroupViewHolder holder = (GroupViewHolder) view.getTag();

        //Set the hour
        int hour = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COL_HOUR));
        holder.hourView.setText(Integer.toString(hour));

        //Set the subject
        String subject = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_SUBJECT));

        //Set the lesson-type (color the subject-TextView)
        String lessonType = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_LESSON_TYPE));
        int background;
        switch (lessonType) {
            case ScheduleEntry.LESSON_TYPE_CANCELED: {
                background = ContextCompat.getColor(context, R.color.lesson_canceled);
                break;
            }
            case ScheduleEntry.LESSON_TYPE_CHANGED: {
                background = ContextCompat.getColor(context, R.color.lesson_changed);
                break;
            }
            case ScheduleEntry.LESSON_TYPE_EXAM: {
                background = ContextCompat.getColor(context, R.color.lesson_exam);
                break;
            }
            case ScheduleEntry.LESSON_TYPE_EVENT: {
                background = ContextCompat.getColor(context, R.color.lesson_event);
                break;
            }
            default: {
                background = ContextCompat.getColor(context, R.color.black_text);
                break;
            }
        }

        holder.subjectsView.setText(subject);
        holder.subjectsView.setTextColor(background);

        //Get the teacher and classroom for the first lesson
        final String teacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_TEACHER));
        final String classroom = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_CLASSROOM));

        //Get the number of lessons
        int lessonCount = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COL_LESSON_COUNT));

        if (lessonCount == 1) { //If there is only one lesson, there is no need for child view
            holder.teacherView.setText(teacher);
            holder.classroomView.setVisibility(View.VISIBLE);
            holder.classroomView.setText(classroom);

            holder.indicatorView.setVisibility(View.GONE);

            view.setOnClickListener(null); //Remove the click listener it might have had, because ListView recycles its views
        } else {
            holder.indicatorView.setVisibility(View.VISIBLE);

            //Reset the holder according to its state
            if (isExpanded) {
                holder.teacherView.setText(teacher);
                holder.classroomView.setVisibility(View.VISIBLE);
                holder.classroomView.setText(classroom);
            } else {
                holder.indicatorView.animate().rotation(0);

                holder.teacherView.setText("...");
                holder.classroomView.setVisibility(View.GONE);
            }

            final int groupPosition = cursor.getPosition();

            //Handle clicks on the group
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isExpanded = mExpandedGroups.get(groupPosition, false);
                    if (isExpanded) { //Needs to collapse
                        holder.indicatorView.animate().rotation(0);
                        mListView.collapseGroup(groupPosition);

                        holder.teacherView.setText("...");
                        holder.classroomView.setVisibility(View.GONE);

                        mExpandedGroups.put(groupPosition, false);
                    } else { //Needs to expand
                        holder.indicatorView.animate().rotation(180);
                        mListView.expandGroup(groupPosition);

                        holder.teacherView.setText(teacher);
                        holder.classroomView.setVisibility(View.VISIBLE);
                        holder.classroomView.setText(classroom);

                        mExpandedGroups.put(groupPosition, true);
                    }
                }
            });
        }
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.schedule_child, parent, false);

        ChildViewHolder holder = new ChildViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {

        ChildViewHolder holder = (ChildViewHolder) view.getTag();

        //Get the data from the cursor
        String subject = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_SUBJECT));
        String classroom = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_CLASSROOM));
        String teacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_TEACHER));
        String lessonType = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COL_LESSON_TYPE));

        holder.subjectView.setText(subject); //Set the subject
        holder.teacherView.setText(teacher); //Set the teacher
        holder.classroomView.setText(classroom); //Set the classroom

        //Set the lesson-type (color the subject-TextView)
        int background;
        switch (lessonType) {
            case ScheduleEntry.LESSON_TYPE_CANCELED: {
                background = ContextCompat.getColor(context, R.color.lesson_canceled);
                break;
            }
            case ScheduleEntry.LESSON_TYPE_CHANGED: {
                background = ContextCompat.getColor(context, R.color.lesson_changed);
                break;
            }
            case ScheduleEntry.LESSON_TYPE_EXAM: {
                background = ContextCompat.getColor(context, R.color.lesson_exam);
                break;
            }
            case ScheduleEntry.LESSON_TYPE_EVENT: {
                background = ContextCompat.getColor(context, R.color.lesson_event);
                break;
            }
            default: {
                background = ContextCompat.getColor(context, R.color.black_text);
                break;
            }
        }

        holder.subjectView.setTextColor(background);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        mExpandedGroups.clear();
    }

    private static class GroupViewHolder {
        private final TextView hourView;
        private final TextView subjectsView;
        private final TextView teacherView;
        private final TextView classroomView;
        private final ImageView indicatorView;

        GroupViewHolder(View view) {
            hourView = (TextView) view.findViewById(R.id.schedule_group_hour);
            subjectsView = (TextView) view.findViewById(R.id.schedule_group_subject);
            teacherView = (TextView) view.findViewById(R.id.schedule_group_teacher);
            classroomView = (TextView) view.findViewById(R.id.schedule_group_classroom);
            indicatorView = (ImageView) view.findViewById(R.id.schedule_group_indicator);
        }
    }

    private static class ChildViewHolder {
        private final TextView subjectView;
        private final TextView classroomView;
        private final TextView teacherView;

        private ChildViewHolder(View view) {
            subjectView = (TextView) view.findViewById(R.id.schedule_child_subject);
            classroomView = (TextView) view.findViewById(R.id.schedule_child_classroom);
            teacherView = (TextView) view.findViewById(R.id.schedule_child_teacher);
        }
    }

    private class ChildLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            int hour = args.getInt(HOUR_KEY);

            //Get the subject, classroom, teacher and lesson type of the specific day and hour from the database
            String[] projection = {
                    ScheduleEntry._ID,
                    ScheduleEntry.COL_SUBJECT,
                    ScheduleEntry.COL_CLASSROOM,
                    ScheduleEntry.COL_TEACHER,
                    ScheduleEntry.COL_LESSON_TYPE};

            String selection =
                    ScheduleEntry.COL_HOUR + " = " + hour + " AND " + //Get data with this hour (id = hour)
                            ScheduleEntry.COL_LESSON + " >= 1"; //And where lesson >= 1, since the group view shows lesson = 0

            String sortOrder = ScheduleEntry.COL_LESSON + " ASC"; //Sort it in ascending order
            Uri uri = ScheduleEntry.buildScheduleWithDayUri(mDay); //Get data with this day

            return new CursorLoader(
                    mContext,
                    uri,
                    projection,
                    selection,
                    null,
                    sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            boolean isCursorNull = getCursor() == null;
            if (!isCursorNull) {
                setChildrenCursor(loader.getId(), data); //Set the cursor of the given group number (id) to the fetched data
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLoadFinished() called" +
                        ", isCursorNull = " + isCursorNull +
                        ", loader id = " + loader.getId());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            boolean isCursorNull = getCursor() == null;
            if (!isCursorNull) {
                setChildrenCursor(loader.getId(), null); //Set the cursor of the given group number (id) to null
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLoadReset() called" +
                        ", isCursorNull = " + isCursorNull +
                        ", loader id = " + loader.getId());
            }
        }
    }
}
