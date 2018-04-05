/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.blackcracks.blich.R;

/**
 * Credits for the school, tech support and developers.
 * Provides support email.
 */
public class AboutActivity extends BaseThemedActivity {

    private static final String[] DEV_EMAIL = {"nukegandhi@gmail.com"};

    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootView = LayoutInflater.from(this).inflate(
                R.layout.activity_about, null , false);
        setContentView(mRootView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_forward_white_24dp);
        }
    }

    /**
     * Open an email message directed to the support email.
     *
     * @param view clicked {@link View}
     */
    public void onEmailClick(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, DEV_EMAIL);
        intent.setData(Uri.parse("mailto:"));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
