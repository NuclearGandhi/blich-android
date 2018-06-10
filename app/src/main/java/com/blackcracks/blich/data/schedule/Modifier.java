package com.blackcracks.blich.data.schedule;

import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.raw.Change;
import com.blackcracks.blich.data.raw.Event;
import com.blackcracks.blich.data.raw.RawExam;
import com.blackcracks.blich.data.raw.RawModifier;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.PreferenceUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;

public class Modifier extends RealmObject {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODIFIER_TYPE_NEW_TEACHER, MODIFIER_TYPE_NEW_HOUR, MODIFIER_TYPE_NEW_ROOM, MODIFIER_TYPE_CANCEL,
            MODIFIER_TYPE_EXAM, MODIFIER_TYPE_EVENT})
    @interface ModifierType {
    }

    public static final int MODIFIER_TYPE_NEW_TEACHER = 0;
    public static final int MODIFIER_TYPE_NEW_HOUR = 1;
    public static final int MODIFIER_TYPE_NEW_ROOM = 2;
    public static final int MODIFIER_TYPE_CANCEL = 3;
    public static final int MODIFIER_TYPE_EXAM = 4;
    public static final int MODIFIER_TYPE_EVENT = 5;

    private @ModifierType int modifierType;
    private String title;

    private String subject;
    private String oldTeacher;
    private String newTeacher;

    private boolean isAReplacer;

    private Date date;
    private int beginPeriod;
    private int endPeriod;

    public Modifier() {}

    public Modifier(@NonNull RawModifier rawModifier) {
        if (rawModifier instanceof Change) {
            Change change = (Change) rawModifier;
            switch (change.getChangeType()) {
                case Database.TYPE_NEW_TEACHER:
                    modifierType = MODIFIER_TYPE_NEW_TEACHER;
                    break;
                case Database.TYPE_NEW_HOUR:
                    modifierType = MODIFIER_TYPE_NEW_HOUR;
                    break;
                case Database.TYPE_NEW_ROOM:
                    modifierType = MODIFIER_TYPE_NEW_ROOM;
                    break;
                case Database.TYPE_CANCELED:
                    modifierType = MODIFIER_TYPE_CANCEL;
            }
        } else if (rawModifier instanceof Event) {
            modifierType = MODIFIER_TYPE_EVENT;
        } else if (rawModifier instanceof RawExam){
            modifierType = MODIFIER_TYPE_EXAM;
        } else {
            throw new IllegalArgumentException("RawModifier is of invalid type: " + rawModifier.getClass());
        }

        title = rawModifier.buildTitle();

        subject = rawModifier.getSubject();
        oldTeacher = rawModifier.getOldTeacher();
        newTeacher = rawModifier.getNewTeacher();

        isAReplacer = rawModifier.isAReplacer();

        date = rawModifier.getDate();
        beginPeriod = rawModifier.getBeginHour();
        endPeriod = rawModifier.getEndHour();
    }

    public @ColorInt
    int getColor() {
        switch (modifierType) {
            case MODIFIER_TYPE_NEW_TEACHER:
            case MODIFIER_TYPE_NEW_HOUR:
            case MODIFIER_TYPE_NEW_ROOM:
                return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_changed_key);
            case MODIFIER_TYPE_CANCEL:
                return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_canceled_key);
            case MODIFIER_TYPE_EXAM:
                return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_exam_key);
            case MODIFIER_TYPE_EVENT:
                return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_event_key);
            default:
                throw new IllegalArgumentException("Invalid modifier type: " + modifierType);
        }
    }

    public boolean isIncludedInPeriod(Period period, @Nullable Calendar instance) {
        return isEqualToPeriod(period.getPeriodNum()) &&
                getDayOfTheWeek(instance) == period.getDay();
    }

    public int getDayOfTheWeek(@Nullable Calendar instance) {
        if (instance == null)
            instance = Calendar.getInstance();

        instance.setTime(date);
        return instance.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Compare between the current lesson and a given hour.
     *
     * @param hour hour - period to compare to.
     * @return {@code true} the lesson is taking place in the given hour.
     */
    public boolean isEqualToPeriod(int hour) {
        return beginPeriod <= hour && hour <= endPeriod;
    }

    public int getModifierType() {
        return modifierType;
    }

    public void setModifierType(int modifierType) {
        this.modifierType = modifierType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAReplacer() {
        return isAReplacer;
    }

    public void setAReplacer(boolean AReplacer) {
        isAReplacer = AReplacer;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBeginPeriod() {
        return beginPeriod;
    }

    public void setBeginPeriod(int beginPeriod) {
        this.beginPeriod = beginPeriod;
    }

    public int getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(int endPeriod) {
        this.endPeriod = endPeriod;
    }
}
