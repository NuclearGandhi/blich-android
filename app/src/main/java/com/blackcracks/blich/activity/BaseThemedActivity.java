/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.activity;/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

import com.afollestad.appthemeengine.ATEActivity;
import com.blackcracks.blich.util.Utilities;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseThemedActivity extends ATEActivity {

    @Override
    public final String getATEKey() {
        return Utilities.getATEKey(this);
    }
}