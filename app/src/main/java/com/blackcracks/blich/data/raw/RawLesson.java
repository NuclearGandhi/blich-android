/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.raw;

import com.blackcracks.blich.data.TeacherSubject;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

/**
 * A data class holding information about a single lesson in an hour.
 */
public class RawLesson extends RealmObject {

    @Required private String subject;
    private String room;
    private String teacher;
    @LinkingObjects("lessons") private final RealmResults<RawPeriod> owners  = null;

    public RawLesson() {}

    public RawLesson(String subject, String room, String teacher) {
        this.subject = subject;
        this.room = room;
        this.teacher = teacher;
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

    /**
     * Create a {@link TeacherSubject} from the lesson.
     *
     * @return a {@link TeacherSubject}.
     */
    public TeacherSubject getTeacherSubject() {
        return new TeacherSubject(teacher, subject);
    }

    public RealmResults<RawPeriod> getOwners() {
        return owners;
    }
}
