package com.blackcracks.blich.data;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Lesson extends RealmObject {

    private int hour;
    @Required private String subject;
    private String room;
    private String teacher;
    private String changeType;

    public Lesson() {

    }

    public Lesson(int hour, String subject, String room, String teacher, String changeType) {
        this.hour = hour;
        this.subject = subject;
        this.room = room;
        this.teacher = teacher;
        this.changeType = changeType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public TeacherSubject getTeacherSubject() {
        return new TeacherSubject(teacher, subject);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }
}
