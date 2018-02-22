package com.blackcracks.blich.listener;

import android.support.annotation.IntDef;
import android.support.design.widget.AppBarLayout;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

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
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            if (mCurrentState != EXPANDED) {
                onStateChanged(EXPANDED);
            }
            mCurrentState = EXPANDED;
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
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

    public abstract void onStateChanged(@State int state);
}