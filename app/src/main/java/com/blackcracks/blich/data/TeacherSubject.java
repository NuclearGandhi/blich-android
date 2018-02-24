/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

/**
 * A class to hold a teacher-subject pair.
 */
public class TeacherSubject {
    private String teacher;
    private String subject;

    public TeacherSubject(String teacher, String subject) {
        this.teacher = teacher;
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TeacherSubject) {
            TeacherSubject ts2 = (TeacherSubject) obj;
            return ts2.getSubject().equals(subject) && ts2.getTeacher().equals(teacher);
        } else {
            return false;
        }
    }
}
