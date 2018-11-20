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
