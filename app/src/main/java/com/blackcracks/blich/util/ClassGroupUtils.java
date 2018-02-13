/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
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
 * Created by Administrator on 2/13/2018.
 */

public class ClassGroupUtils {

    public static int getClassValue(Context context) {
        return PreferencesUtils.getInt(context,
                Constants.Preferences.PREF_USER_CLASS_GROUP_KEY);
    }

    public static String[] loadDataIntoPicker(
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

        //Load values when grade changes
        gradePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal < 4) { //Normal grade
                    classIndexPicker.setVisibility(View.VISIBLE);
                    classIndexPicker.setMaxValue(maxIndexes[newVal]);
                } else { //Abnormal
                    classIndexPicker.setVisibility(View.INVISIBLE);
                }
            }
        });

        ClassGroup classGroup = RealmUtils.getGrade(realm, currentUserClassGroupId);

        //Set current value
        if (classGroup.isNormal()) {
            int grade = classGroup.getGrade();
            int number = classGroup.getNumber();

            gradePicker.setValue(grade - 9);

            classIndexPicker.setValue(number);
        } else {
            gradePicker.setValue(4);
        }

        return displayedValues;
    }


    public static int[] fetchMaxIndices(Realm realm) {
        int[] classMaxIndex = new int[4];
        for (int i = 0; i < classMaxIndex.length; i++) {
            classMaxIndex[i] = maxIndexFromGrade(realm, i + 9);
        }

        return classMaxIndex;
    }

    public static List<ClassGroup> fetchAbnormalClasses(Realm realm) {
        return realm.where(ClassGroup.class)
                .equalTo("grade", 0)
                .and()
                .equalTo("number", 0)
                .findAll();
    }

    private static int maxIndexFromGrade(Realm realm, int grade) {
        RealmResults<ClassGroup> results = realm.where(ClassGroup.class)
                .equalTo("grade", grade)
                .findAll();

        return results.max("number").intValue();
    }
}
