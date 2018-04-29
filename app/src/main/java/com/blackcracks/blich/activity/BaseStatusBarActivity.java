package com.blackcracks.blich.activity;

import android.support.v7.app.AppCompatActivity;

import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.util.Utilities;

public class BaseStatusBarActivity extends AppCompatActivity {

    private boolean mAutoStatusBarColor = true;

    public void setAutoStatusBarColor(boolean autoStatusBarColor) {
        mAutoStatusBarColor = autoStatusBarColor;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAutoStatusBarColor) getWindow().setStatusBarColor(Config.primaryColorDark(
                this,
                Utilities.getATEKey(this)));
    }
}
