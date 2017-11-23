package com.blackcracks.blich.data;

/**
 * A base class for each lesson in the Schedule Table
 */
public class Lesson {

    private int mDay;
    private int mHour;
    private String mSubject;
    private String mClassroom;
    private String mTeacher;
    private String mLessonType;

    public Lesson(String subject, String teacher, String classroom, String lessonType) {
        mSubject = subject;
        mTeacher = teacher;
        mClassroom = classroom;
        mLessonType = lessonType;
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
}
