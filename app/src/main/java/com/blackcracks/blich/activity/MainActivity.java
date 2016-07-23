package com.blackcracks.blich.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.blackcracks.blich.R;
import com.blackcracks.blich.fragment.ScheduleFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String SCHEDULE_FRAGMENT_TAG = "schedule_fragment_tag";

    private ScheduleFragment mScheduleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        if (savedInstanceState != null) {
            mScheduleFragment = (ScheduleFragment) getSupportFragmentManager().
                    getFragment(savedInstanceState, SCHEDULE_FRAGMENT_TAG);
        } else {
            mScheduleFragment = new ScheduleFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, mScheduleFragment, SCHEDULE_FRAGMENT_TAG)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState,
                SCHEDULE_FRAGMENT_TAG,
                mScheduleFragment);
    }
}
