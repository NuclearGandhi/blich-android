package com.blackcracks.blich.data;

import android.support.annotation.NonNull;

/**
 * A base class for each lesson in the Schedule Table
 */
public class Lesson implements Comparable<Lesson> {

    private int mClassSettings;
    private int mDay;
    private int mHour;
    private String mSubject;
    private String mClassroom;
    private String mTeacher;
    private String mLessonType;

    public Lesson(int classSettings, int day, int hour, String subject, String lessonType) {
        mClassSettings = classSettings;
        mDay = day;
        mHour = hour;
        mSubject = subject;
        mLessonType = lessonType;
    }

    public int getDay() {
        return mDay;
    }

    public void setDay(int day) {
        this.mDay = day;
    }

    public int getClassSettings() {
        return mClassSettings;
    }

    public void setClassSettings(int classSettings) {
        this.mClassSettings = classSettings;
    }

    public int getHour() {
        return mHour;
    }

    public void setHour(int hour) {
        this.mHour = hour;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public String getClassroom() {
        return mClassroom;
    }

    public void setClassroom(String classroom) {
        this.mClassroom = classroom;
    }

    public String getTeacher() {
        return mTeacher;
    }

    public void setTeacher(String teacher) {
        this.mTeacher = teacher;
    }

    public String getLessonType() {
        return mLessonType;
    }

    public void setLessonType(String lessonType) {
        this.mLessonType = lessonType;
    }

    @Override
    public int compareTo(@NonNull Lesson o) {
        if (mDay == o.getDay()) {
            if (mHour == o.getHour()) return 0;
            else if (mHour > o.getHour()) return 1;
            else return -1;
        } else if (mDay > o.getDay()) return 1;
        else return -1;
    }
}
