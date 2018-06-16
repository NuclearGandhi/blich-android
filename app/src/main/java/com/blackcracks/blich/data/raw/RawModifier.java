/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.raw;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

import com.blackcracks.blich.data.schedule.Lesson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * An interface for all changes in the schedule and their common behavior.
 */
public abstract class RawModifier {

    protected Date date;
    protected int beginHour;
    protected int endHour;

    protected String title;
    protected String subject;
    protected String oldTeacher;
    protected String oldRoom;

    /**
     * Build a label to show to the user.
     *
     * @return a label.
     */
    public String buildTitle() {
        String str = title;
        if (oldTeacher != null && !oldTeacher.equals("")) str += " לקבוצה של " + oldTeacher;
        if (oldRoom != null && !oldRoom.equals("")) str += " בחדר " + oldRoom;
        return str;
    }

    public boolean isInWeek(int weekOffset) {
        Calendar today = Calendar.getInstance();

        Calendar thisDate = Calendar.getInstance();
        thisDate.setTime(date);
        today.add(Calendar.WEEK_OF_YEAR, weekOffset);

        return thisDate.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * @return {@code true} the lesson replaces another lesson.
     * {@code false} the lesson comes in addition to other lessons.
     */
    public boolean isAReplacer() {
        return (oldTeacher != null && subject != null) &&
                !oldTeacher.equals("") && !subject.equals("");
    }

    /**
     * Can the current lesson replace the given {@link RawLesson}.
     *
     * @param toReplace {@link RawLesson} to replace
     * @return {@code true} replaces the given {@link RawLesson}.
     */
    public boolean isAReplacer(Lesson toReplace) {
        return (oldTeacher != null && subject != null) &&
                oldTeacher.equals(toReplace.getTeacher()) && subject.equals(toReplace.getSubject());
    }

    /**
     * Get a color representing the lesson.
     *
     * @return a {@link ColorInt}.
     */
    @ColorInt
    public abstract int getColor();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RawModifier) {
            RawModifier e = (RawModifier) obj;
            return buildTitle().equals(e.buildTitle());
        }
        return false;
    }

    public int getDayOfTheWeek(@Nullable Calendar instance) {
        if (instance == null)
            instance = Calendar.getInstance();

        instance.setTime(date);
        return instance.get(Calendar.DAY_OF_WEEK);
    }

    public static <T extends RawModifier> List<T> extractType(List<RawModifier> rawModifiers, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (RawModifier rawModifier : rawModifiers)
            if (type.isInstance(rawModifier)) {
                result.add((T) rawModifier);
            }

        return result;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
        if (beginHour == 0)
            beginHour = 1;
        this.beginHour = beginHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOldTeacher() {
        return oldTeacher;
    }

    public void setOldTeacher(String oldTeacher) {
        this.oldTeacher = oldTeacher;
    }

    public String getOldRoom() {
        return oldRoom;
    }

    public void setOldRoom(String oldRoom) {
        this.oldRoom = oldRoom;
    }

    public String getNewTeacher() {
        return null;
    }

    public String getNewRoom() {
        return null;
    }
}
