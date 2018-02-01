/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants.Database;

import java.util.Date;

public class Change extends DatedLesson {

    private String changeType;
    private int hour;

    private String newTeacher;
    private String newRoom;
    private int newHour;

    public Change() {
    }

    public Change(Date date, String subject, String teacher, String changeType, int hour) {
        super(date, subject, teacher);
        setChangeType(changeType);
        setHour(hour);
    }

    @Override
    public String buildName() {
        String str = "";
        switch (getChangeType()) {
            case Database.TYPE_CANCELED: {
                str = "ביטול " + buildLessonName();
                break;
            }
            case Database.TYPE_NEW_HOUR: {
                str = "הזזת " + buildLessonName() + " לשעה " + getNewHour();
                break;
            }
            case Database.TYPE_NEW_ROOM: {
                str = buildLessonName() + " -> חדר: " + getNewRoom();
                break;
            }
            case Database.TYPE_NEW_TEACHER: {
                str = buildLessonName() + " -> מורה: " + getNewTeacher();
                break;
            }
        }

        return str;
    }

    @Override
    public String getType() {
        return changeType;
    }

    @Override
    public int getColor(Context context) {
        int colorId;
        if (changeType.equals(Database.TYPE_CANCELED)) colorId = R.color.lesson_canceled;
        else colorId = R.color.lesson_changed;

        return ContextCompat.getColor(context, colorId);
    }

    @Override
    public boolean isEqualToHour(int hour) {
        return this.hour == hour;
    }

    private String buildLessonName() {
        return getSubject() + ", " + getTeacher();
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public String getNewTeacher() {
        return newTeacher;
    }

    public void setNewTeacher(String newTeacher) {
        this.newTeacher = newTeacher;
    }

    public String getNewRoom() {
        return newRoom;
    }

    public void setNewRoom(String newRoom) {
        this.newRoom = newRoom;
    }

    public int getNewHour() {
        return newHour;
    }

    public void setNewHour(int newHour) {
        this.newHour = newHour;
    }

    @Override
    public int compareTo(@NonNull DatedLesson o) {
        return 0;
    }
}
