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

package com.blackcracks.blich.widget.listener;

import android.support.annotation.IntDef;
import android.support.design.widget.AppBarLayout;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A listener to the {@link AppBarLayout}'s state.
 */
public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    @Retention(SOURCE)
    @IntDef({EXPANDED,
            COLLAPSED,
            IDLE})
    protected @interface State{}

    protected static final int EXPANDED = 0;
    protected static final int COLLAPSED = 1;
    private static final int IDLE = 2;

    private @State int mCurrentState = IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            if (mCurrentState != EXPANDED) {
                onStateChanged(EXPANDED);
            }
            mCurrentState = EXPANDED;
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != COLLAPSED) {
                onStateChanged(COLLAPSED);
            }
            mCurrentState = COLLAPSED;
        } else {
            if (mCurrentState != IDLE) {
                onStateChanged(IDLE);
            }
            mCurrentState = IDLE;
        }
    }

    /**
     * Called when the {@link AppBarLayout}'s state changes.
     * @param state The new state.
     */
    public abstract void onStateChanged(@State int state);
}