package com.blackcracks.blich.data.schedule;

import android.os.Parcel;
import android.support.annotation.ColorInt;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Lesson extends RealmObject {

    private String subject;
    private String room;
    private String teacher;
    private Modifier modifier;

    @LinkingObjects("lessons")
    private final RealmResults<Period> owners = null;

    @LinkingObjects("firstLesson")
    private final RealmResults<Period> otherOwners = null;

    public Lesson() {}

    public Lesson(String subject, String teacher, String room) {
        this.subject = subject;
        this.room = room;
        this.teacher = teacher;
    }

    protected Lesson(Parcel in) {
        subject = in.readString();
        room = in.readString();
        teacher = in.readString();
    }

    public String buildTitle() {
        if (modifier != null)
            return modifier.getTitle();
        else
            return subject;
    }

    public @ColorInt int getColor() {
        if (modifier != null)
            return modifier.getColor();
        else
            return -1;
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

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public RealmResults<Period> getOwners() {
        return owners;
    }

    public RealmResults<Period> getOtherOwners() {
        return otherOwners;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Lesson) {
            Lesson o = (Lesson) obj;
            return teacher.equals(o.getTeacher()) && subject.equals(o.getSubject());
        }
        return false;
    }
}
