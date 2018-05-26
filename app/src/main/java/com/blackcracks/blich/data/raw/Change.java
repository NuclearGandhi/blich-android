/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.raw;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.PreferenceUtils;

/**
 * A data class holding information about a change in schedule.
 */
public class Change extends RawModifier {

    private String changeType;

    private String newTeacher;
    private String newRoom;
    private int newHour;

    public Change() {
    }

    public Change cloneNewHourVariant() {
        Change newChange = new Change();
        newChange.setDate(date);
        newChange.setBeginHour(newHour);
        newChange.setEndHour(newHour);

        newChange.setTitle(title);
        newChange.setSubject(subject);
        newChange.setOldTeacher(oldTeacher);
        newChange.setOldRoom(oldRoom);

        newChange.setChangeType(changeType);
        newChange.setNewTeacher(newTeacher);
        newChange.setNewRoom(oldRoom);
        newChange.setNewHour(newHour);

        return newChange;
    }

    @Override
    public String buildTitle() {
        switch (changeType) {
            case Database.TYPE_CANCELED: {
                return "ביטול " + buildBaseTitle();
            }
            case Database.TYPE_NEW_HOUR: {
                return "הזזת " + buildBaseTitle() + " לשעה " + newHour;
            }
            case Database.TYPE_NEW_ROOM: {
                return buildBaseTitle() + " -> חדר: " + newRoom;
            }
            case Database.TYPE_NEW_TEACHER: {
                return buildBaseTitle() + " -> מורה: " + newTeacher;
            }
            default: {
                return buildBaseTitle();
            }
        }
    }

    @Override
    public int getColor() {
        if (changeType.equals(Database.TYPE_CANCELED)) {
            return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_canceled_key);
        } else {
            return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_changed_key);
        }
    }

    private String buildBaseTitle() {
        return subject + ", " + oldTeacher;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setNewTeacher(String newTeacher) {
        this.newTeacher = newTeacher;
    }

    public void setNewRoom(String newRoom) {
        this.newRoom = newRoom;
    }

    public void setNewHour(int newHour) {
        this.newHour = newHour;
    }

    @Override
    public String getNewTeacher() {
        return newTeacher;
    }

    @Override
    public String getNewRoom() {
        return newRoom;
    }
}
