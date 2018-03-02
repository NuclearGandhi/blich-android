/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.view.View;
import android.widget.NumberPicker;

import com.blackcracks.blich.data.ClassGroup;

import java.util.List;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A utility class for {@link ClassGroup}s.
 */
public class ClassGroupUtils {

    /**
     * Get the user's {@link ClassGroup} id.
     *
     * @return a {@link ClassGroup} id.
     */
    public static int getClassValue(Context context) {
        return PreferencesUtils.getInt(context,
                Constants.Preferences.PREF_USER_CLASS_GROUP_KEY);
    }

    /**
     * Insert data from {@link ClassGroup}s into {@link MaterialNumberPicker}s.
     *
     * @param realm a {@link Realm} instance.
     * @param gradePicker a picker for grades.
     * @param classIndexPicker a picker for class indices.
     * @param currentUserClassGroupId the current user's {@link ClassGroup} id.
     */
    public static void loadDataIntoPicker(
            Realm realm,
            MaterialNumberPicker gradePicker,
            final MaterialNumberPicker classIndexPicker,
            int currentUserClassGroupId) {

        final int[] maxIndexes = ClassGroupUtils.fetchMaxIndices(realm);
        List<ClassGroup> abnormalClasses = ClassGroupUtils.fetchAbnormalClasses(realm);

        String[] displayedValues = new String[4 + abnormalClasses.size()];
        displayedValues[0] = "ט";
        displayedValues[1] = "י";
        displayedValues[2] = "יא";
        displayedValues[3] = "יב";
        for (int i = 0; i < abnormalClasses.size(); i++) {
            displayedValues[i + 4] = abnormalClasses.get(i).getName();
        }

        gradePicker.setDisplayedValues(displayedValues);
        gradePicker.setMaxValue(displayedValues.length - 1);

        //Load values when grade changes
        NumberPicker.OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal < 4) { //Normal grade
                    classIndexPicker.setVisibility(View.VISIBLE);
                    classIndexPicker.setMaxValue(maxIndexes[newVal]);
                } else { //Abnormal
                    classIndexPicker.setVisibility(View.INVISIBLE);
                }
            }
        };
        gradePicker.setOnValueChangedListener(valueChangeListener);

        ClassGroup classGroup = RealmUtils.getGrade(realm, currentUserClassGroupId);

        //Set current value
        int gradeIndex;
        if (classGroup.isNormal()) {
            int grade = classGroup.getGrade();
            int number = classGroup.getNumber();

            gradeIndex = grade - 9;
            classIndexPicker.setValue(number);
        } else {
            gradeIndex = 4;
        }
        gradePicker.setValue(gradeIndex);
        valueChangeListener.onValueChange(gradePicker, 1, gradeIndex);
    }


    /**
     * Get all the max grade indices for each grade.
     *
     * @return int array of max grade indices.
     */
    private static int[] fetchMaxIndices(Realm realm) {
        int[] classMaxIndex = new int[4];
        for (int i = 0; i < classMaxIndex.length; i++) {
            classMaxIndex[i] = maxIndexFromGrade(realm, i + 9);
        }

        return classMaxIndex;
    }

    /**
     * Get max index for specified grade
     *
     * @param grade a grade.
     * @return the max index.
     */
    private static int maxIndexFromGrade(Realm realm, int grade) {
        RealmResults<ClassGroup> results = realm.where(ClassGroup.class)
                .equalTo("grade", grade)
                .findAll();

        //noinspection ConstantConditions
        return results.max("number").intValue();
    }

    /**
     * Fetch all the abnormal {@link ClassGroup}s in the database.
     *
     * @return a list of {@link ClassGroup}s.
     */
    private static List<ClassGroup> fetchAbnormalClasses(Realm realm) {
        return realm.where(ClassGroup.class)
                .equalTo("grade", 0)
                .and()
                .equalTo("number", 0)
                .findAll();
    }
}
