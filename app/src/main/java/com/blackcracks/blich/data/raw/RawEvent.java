/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.raw;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.PreferenceUtils;

/**
 * A data class holding information about events.
 */
public class RawEvent extends RawModifier {

    @Override
    public int getColor() {
        return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_event_key);
    }
}
