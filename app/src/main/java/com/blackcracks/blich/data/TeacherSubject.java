/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
