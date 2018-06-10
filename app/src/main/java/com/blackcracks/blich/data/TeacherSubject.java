package com.blackcracks.blich.data;

import com.blackcracks.blich.data.raw.RawLesson;

import io.realm.RealmObject;

public class TeacherSubject extends RealmObject {

    private String teacher;
    private String subject;

    public TeacherSubject() {}

    public TeacherSubject(String teacher, String subject) {
        this.teacher = teacher;
        this.subject = subject;
    }

    public TeacherSubject(RawLesson rawLesson) {
        teacher = rawLesson.getTeacher();
        subject = rawLesson.getSubject();
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
            TeacherSubject ts = (TeacherSubject) obj;
            return ts.subject.equals(subject) && ts.teacher.equals(teacher);
        }

        return false;
    }
}
