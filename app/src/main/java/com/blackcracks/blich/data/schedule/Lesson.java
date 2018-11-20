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
