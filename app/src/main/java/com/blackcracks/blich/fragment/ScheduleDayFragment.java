package com.blackcracks.blich.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ScheduleAdapter;
import com.blackcracks.blich.data.BlichContract.LessonEntry;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;

/**
 * The ScheduleDayFragment is the fragment in each one of the pages of the ScheduleFragment
 */
public class ScheduleDayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DAY_KEY = "day";

    private ScheduleAdapter mAdapter;
    private int mDay;
    private TextView mStatusTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments() != null ? getArguments().getInt(DAY_KEY) : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_day, container, false);

        ExpandableListView listView = rootView.findViewById(R.id.expandable_listview_schedule_day);
        mAdapter = new ScheduleAdapter(null, getContext(), listView, getLoaderManager(), mDay);
        listView.setAdapter(mAdapter);
        listView.setChildDivider(ContextCompat.getDrawable(getContext(), android.R.color.transparent)); //Hide the child dividers

        ViewCompat.setNestedScrollingEnabled(listView, true);

        mStatusTextView = rootView.findViewById(R.id.text_status);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(Constants.SCHEDULE_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                LessonEntry._ID,
                ScheduleEntry.TABLE_NAME + "." + ScheduleEntry.COL_HOUR,
                ScheduleEntry.COL_LESSON_COUNT,
                ScheduleEntry.COL_EVENTS,
                LessonEntry.COL_SUBJECT,
                LessonEntry.COL_CLASSROOM,
                LessonEntry.COL_TEACHER,
                LessonEntry.COL_LESSON_TYPE
        };

        String selection = ScheduleEntry.TABLE_NAME + "." + ScheduleEntry.COL_DAY + " = " + mDay;

        selection += Utilities.Sqlite.generateFilterCondition(getContext());

        String sortOrder = ScheduleEntry.TABLE_NAME + "." + ScheduleEntry.COL_HOUR + " ASC";
        Uri uri = ScheduleEntry.buildScheduleWithLessonUri(0);

        return new CursorLoader(
                getContext(),
                uri,
                projection,
                selection,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        mAdapter.changeCursor(data);

        if (data.getCount() == 0 && !Utilities.isFirstLaunch(getContext())) {
            @BlichSyncAdapter.FetchStatus int status = Utilities.getPreferenceInt(getContext(),
                    getContext().getString(R.string.pref_fetch_status_key),
                    BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL);

            mStatusTextView.setVisibility(View.VISIBLE);

            if (status == BlichSyncAdapter.FETCH_STATUS_SUCCESSFUL)
                mStatusTextView.setText(R.string.blank_page_message_empty);
            else
                mStatusTextView.setText(R.string.blank_page_message_failed);
        }

        mStatusTextView.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.changeCursor(null);
    }
}
