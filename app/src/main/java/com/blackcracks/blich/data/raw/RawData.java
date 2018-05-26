package com.blackcracks.blich.data.raw;

import java.util.ArrayList;
import java.util.List;

public class RawData {

    private List<RawPeriod> rawPeriods;
    private List<RawModifier> mRawModifiers;

    public RawData() {
        mRawModifiers = new ArrayList<>();
    }

    public List<RawPeriod> getRawPeriods() {
        return rawPeriods;
    }

    public void setRawPeriods(List<RawPeriod> rawPeriods) {
        this.rawPeriods = rawPeriods;
    }

    public void addModifiedLessons(List<? extends RawModifier> modifiedLessonList) {
        mRawModifiers.addAll(modifiedLessonList);
    }

    public List<RawModifier> getRawModifiers() {
        return mRawModifiers;
    }
}
