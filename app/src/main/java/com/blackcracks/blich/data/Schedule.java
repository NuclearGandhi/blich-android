/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Schedule extends RealmObject {

    private int classId;
    @Required private RealmList<Hour> hours;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public RealmList<Hour> getHours() {
        return hours;
    }

    public void setHours(RealmList<Hour> hours) {
        this.hours = hours;
    }
}
