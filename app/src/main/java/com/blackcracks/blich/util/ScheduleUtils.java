/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import java.util.Calendar;

public class ScheduleUtils {

    public static int getWantedDayOfTheWeek() {

        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 18 && day != 7)
            day++; //Move to the next day if it is later than 18:00, unless it is Saturday.
        if (day == 7) day = 1; //If it is Saturday, set day to 1 (Sunday).
        return day;
    }
}
