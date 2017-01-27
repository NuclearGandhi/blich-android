package com.blackcracks.blich.data;

import android.content.ContentValues;

import com.blackcracks.blich.data.BlichContract.*;

public class Exam implements Comparable<Exam> {

    private String mDate;
    private String mSubject;
    private int mStartHour;
    private int mEndHour;
    private String mTeacher;
    private String mRoom;

    public Exam(String room,
                String date,
                String subject,
                int startHour,
                int endHour,
                String teacher) {
        mRoom = room;
        mDate = date;
        mSubject = subject;
        mStartHour = startHour;
        mEndHour = endHour;
        mTeacher = teacher;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        mSubject = subject;
    }

    public int getStartHour() {
        return mStartHour;
    }

    public void setStartHour(int startHour) {
        mStartHour = startHour;
    }

    public int getEndHour() {
        return mEndHour;
    }

    public void setEndHour(int endHour) {
        mEndHour = endHour;
    }

    public String getTeacher() {
        return mTeacher;
    }

    public void setTeacher(String teacher) {
        mTeacher = teacher;
    }

    public String getRoom() {
        return mRoom;
    }

    public void setRoom(String room) {
        mRoom = room;
    }

    @Override
    public int compareTo(Exam exam) {
        if (
                mDate.equals(exam.getDate()) &&
                mSubject.equals(exam.getSubject()) &&
                mStartHour == exam.getStartHour() &&
                mEndHour == exam.getEndHour())  {
            return 0;
        }
        return -1;
    }

    public void addToContentValues(ContentValues contentValues) {
        contentValues.put(ExamsEntry.COL_DATE, mDate);
        contentValues.put(ExamsEntry.COL_SUBJECT, mSubject);
        contentValues.put(ExamsEntry.COL_START_HOUR, mStartHour);
        contentValues.put(ExamsEntry.COL_END_HOUR, mEndHour);
        contentValues.put(ExamsEntry.COL_TEACHER, mTeacher);
        contentValues.put(ExamsEntry.COL_ROOM, mRoom);
    }
}
