/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

/**
 * A data class holding information about a single lesson in an hour.
 */
public class Lesson extends RealmObject {

    @Required private String subject;
    private String room;
    private String teacher;
    private String changeType;
    @LinkingObjects("lessons") private final RealmResults<Hour> owners  = null;

    public Lesson() {

    }

    public Lesson(String subject, String room, String teacher, String changeType) {
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

    /**
     * Create a {@link TeacherSubject} from the lesson.
     *
     * @return a {@link TeacherSubject}.
     */
    public TeacherSubject getTeacherSubject() {
        return new TeacherSubject(teacher, subject);
    }

    public RealmResults<Hour> getOwners() {
        return owners;
    }
}
