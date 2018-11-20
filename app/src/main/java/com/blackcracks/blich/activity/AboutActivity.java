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

package com.blackcracks.blich.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.RealmUtils;

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

        RealmUtils.setUpRealm(this);

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
