package com.blackcracks.blich.data.schedule;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.data.raw.ModifiedLesson;

import io.realm.annotations.Required;

public class Lesson implements Parcelable {

    @Required private String subject;
    private String room;
    private String teacher;
    private ModifiedLesson modifier;

    private Period owner;

    public Lesson(String subject, String room, String teacher) {
        this.subject = subject;
        this.room = room;
        this.teacher = teacher;
    }

    protected Lesson(Parcel in) {
        subject = in.readString();
        room = in.readString();
        teacher = in.readString();
        owner = in.readParcelable(Period.class.getClassLoader());
    }

    public String buildTitle() {
        if (modifier != null)
            return modifier.buildName();
        else
            return subject;
    }

    public @ColorInt int getColor() {
        if (modifier != null)
            return modifier.getColor();
        else
            return -1;
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };

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

    public ModifiedLesson getModifier() {
        return modifier;
    }

    public void setModifier(ModifiedLesson modifier) {
        this.modifier = modifier;
    }

    public Period getOwner() {
        return owner;
    }

    public void setOwner(Period owner) {
        this.owner = owner;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject);
        dest.writeString(room);
        dest.writeString(teacher);
        dest.writeParcelable(owner, flags);
    }
}
