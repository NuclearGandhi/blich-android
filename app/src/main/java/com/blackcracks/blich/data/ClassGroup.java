/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

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
