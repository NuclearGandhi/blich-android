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

public class MonthDivider implements ExamItem{

    private Date date;

    public MonthDivider(Date date) {
        this.date = date;
    }

    public String buildLabel() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Locale locale = new Locale("iw");
        String year = calendar.getDisplayName(Calendar.YEAR, Calendar.LONG, locale);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
        return month + " " + year;
    }

    @Override
    public int getType() {
        return ExamItem.TYPE_MONTH;
    }
}
