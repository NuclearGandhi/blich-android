/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.widget;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FillingTabLayout extends TabLayout {
    public FillingTabLayout(Context context) {
        super(context);
    }

    public FillingTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FillingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //Fix the tabs to take the whole width of the screen
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            if (getTabCount() == 0)
                return;
            ViewGroup tabStrip = (ViewGroup) getChildAt(0);
            int minWidth = getMeasuredWidthAndState() / getTabCount();
            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                View child = tabStrip.getChildAt(i);
                child.setMinimumWidth(minWidth);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}