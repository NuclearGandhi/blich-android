package com.blackcracks.blich.data;

import com.blackcracks.blich.util.PreferenceUtils;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class ExamReminder extends RealmObject {
    @Required private Exam exam;
    private Date remindDate;
    private int requestCode;

    public ExamReminder() {}

    public ExamReminder(Exam exam, Date remindDate, int requestCode) {
        this.exam = exam;
        this.remindDate = remindDate;
        this.requestCode = requestCode;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public Date getRemindDate() {
        return remindDate;
    }

    public void setRemindDate(Date remindDate) {
        this.remindDate = remindDate;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }
}