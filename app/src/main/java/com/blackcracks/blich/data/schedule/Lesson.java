package com.blackcracks.blich.data.schedule;

import android.os.Parcel;
import android.os.Parcelable;

import com.blackcracks.blich.data.raw.RawPeriod;

import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

public class Lesson implements Parcelable {

    @Required private String subject;
    private String room;
    private String teacher;
    private ModifiedLesson modifier;

    private Period owner;

    protected Lesson(Parcel in) {
        subject = in.readString();
        room = in.readString();
        teacher = in.readString();
        owner = in.readParcelable(Period.class.getClassLoader());
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
