package com.blackcracks.blich.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ScheduleAdapter;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;

/**
 * The ScheduleDayFragment is the fragment in each one of the pages of the ScheduleFragment
 */
public class ScheduleDayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SCHEDULE_LOADER_ID = 100;

    private static final String[] SCHEDULE_COLUMNS = {
            ScheduleEntry._ID,
            ScheduleEntry.COL_CLASS_SETTINGS,
            ScheduleEntry.COL_DAY,
            ScheduleEntry.COL_HOUR,
            ScheduleEntry.COL_SUBJECT,
            ScheduleEntry.COL_CLASSROOM,
            ScheduleEntry.COL_TEACHER,
            ScheduleEntry.COL_LESSON_TYPE
    };

    @SuppressWarnings("unused")
    public static final int COL_ID = 0;
    @SuppressWarnings("unused")
    public static final int COL_CLASS_SETTINGS = 1;
    @SuppressWarnings("unused")
    public static final int COL_DAY = 2;
    public static final int COL_HOUR = 3;
    public static final int COL_SUBJECT = 4;
    public static final int COL_CLASSROOM = 5;
    public static final int COL_TEACHER = 6;
    public static final int COL_LESSON_TYPE = 7;

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

        ListView listView = (ListView) rootView.findViewById(R.id.listview_schedule_day);
        mAdapter = new ScheduleAdapter(getContext(), null, 0);
        listView.setAdapter(mAdapter);

        ViewCompat.setNestedScrollingEnabled(listView, true);

        mStatusTextView = (TextView) rootView.findViewById(R.id.text_status);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SCHEDULE_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = ScheduleEntry.COL_HOUR + " ASC";
        Uri uri = ScheduleEntry.buildScheduleWithDayUri(mDay);

        return new CursorLoader(
                getContext(),
                uri,
                SCHEDULE_COLUMNS,
                null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        mAdapter.swapCursor(data);

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
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }
}
