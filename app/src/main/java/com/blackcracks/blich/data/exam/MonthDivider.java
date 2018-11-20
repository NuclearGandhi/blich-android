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

package com.blackcracks.blich.data.exam;

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
    public @ExamItemType
    int getType() {
        return ExamItem.TYPE_MONTH;
    }
}
