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

package com.blackcracks.blich.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.blackcracks.blich.fragment.ScheduleDayFragment;

/**
 * A {@link FragmentStatePagerAdapter} containing 6 tabs each representing the school days of the week.
 * It handles right to left tab layout.
 */
public class SchedulePagerAdapter extends FragmentStatePagerAdapter {

    private static final int TABS_COUNT = 6;

    private final String[] mTabNames;

    /**
     * @param tabNames An array representing the tab titles.
     */
    public SchedulePagerAdapter(FragmentManager fm,
                                String[] tabNames) {
        super(fm);
        mTabNames = tabNames;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt(ScheduleDayFragment.DAY_KEY, getRealPosition(position) + 1);
        ScheduleDayFragment fragment = new ScheduleDayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return TABS_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabNames[getRealPosition(position)];
    }

    /**
     * Get the "real" position, from the old position
     * The adapter is set to left to right, but acts like a right to left,
     * making the positions inverted. This method is used to get the "real" position of a page.
     *
     * @param position old position to convert to the "real" position
     * @return the "real" position.
     */
    public static int getRealPosition(int position) {
        return TABS_COUNT - 1 - position;
    }
}
