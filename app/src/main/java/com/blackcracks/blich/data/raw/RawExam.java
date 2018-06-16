/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.raw;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.PreferenceUtils;

/**
 * A data class holding information about exams.
 */
public class RawExam extends RawModifier {

    public RawExam() {}

    @Override
    public int getColor() {
        return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_exam_key);
    }

    public String getBaseTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (title.contains("מבחן") ||
                title.contains("בוחן") ||
                title.contains("מבחני") ||
                title.contains("מתכונת") ||
                title.contains("בגרות")) {
            this.title = title;
        } else {
            this.title = "מבחן ב" + title;
        }
    }
}
