/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.view;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

//TODO remove this view
public class RtlTabLayout extends TabLayout {
    public RtlTabLayout(Context context) {
        super(context);
    }

    public RtlTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RtlTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                View child = tabStrip.getChildAt(i);
                int minWidth = (int) (getMeasuredWidth() / (float) getTabCount());
                child.setMinimumWidth(minWidth);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}