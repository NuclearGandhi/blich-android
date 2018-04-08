/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.appwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ScheduleRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScheduleRemoteViewsFactory(getBaseContext());
    }
}