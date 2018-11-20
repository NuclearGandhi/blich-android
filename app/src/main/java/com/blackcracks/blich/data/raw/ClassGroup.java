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

package com.blackcracks.blich.data.raw;

import io.realm.RealmObject;

/**
 * A data class holding information about a class-group.
 */
public class ClassGroup extends RealmObject{

    private int id;
    private String name;
    private int grade;
    private int number;

    public ClassGroup() {}

    public ClassGroup(int id, String name, int grade, int number) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.number = number;
    }

    public static String gradeNumToString(int grade) {
        switch (grade) {
            case 9: return "ט";
            case 10: return "י";
            case 11: return "יא";
            case 12: return "יב";
        }
        throw new IllegalArgumentException("Input must be between 9 and 12");
    }

    public static int gradeStringToNum(String grade) {
        switch (grade) {
            case "ט": return 9;
            case "י": return 10;
            case "יא":return 11;
            case "יב": return 12;
        }
        throw new IllegalArgumentException("Input must be one of the following ט ,י ,יא ,יב");
    }

    public boolean isNormal() {
        return grade != 0 && number != 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
