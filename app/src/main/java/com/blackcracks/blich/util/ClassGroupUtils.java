/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import com.blackcracks.blich.data.raw.ClassGroup;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A utility class for {@link ClassGroup}s.
 */
public class ClassGroupUtils {

    /**
     * Get all the max grade indices for each grade.
     *
     * @return int array of max grade indices.
     */
    public static int[] fetchMaxIndices(Realm realm) {
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

        Number number = results.max("number");
        return number != null ? number.intValue() : 1;
    }

    /**
     * Fetch all the abnormal {@link ClassGroup}s in the database.
     *
     * @return a list of {@link ClassGroup}s.
     */
    public static List<ClassGroup> fetchAbnormalClasses(Realm realm) {
        return realm.where(ClassGroup.class)
                .equalTo("grade", 0)
                .and()
                .equalTo("number", 0)
                .findAll();
    }
}
