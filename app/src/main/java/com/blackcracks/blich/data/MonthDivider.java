/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A divider to be used in {@link com.blackcracks.blich.adapter.ExamAdapter}.
 */
public class MonthDivider implements ExamItem{

    private Date date;

    public MonthDivider(Date date) {
        this.date = date;
    }

    /**
     * Build a label to be displayed to the user.
     *
     * @return a label.
     */
    public String buildLabel() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Locale locale = new Locale("iw");
        int year = calendar.get(Calendar.YEAR);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
        return month + " " + year;
    }

    @Override
    public @Type int getType() {
        return ExamItem.TYPE_MONTH;
    }
}
