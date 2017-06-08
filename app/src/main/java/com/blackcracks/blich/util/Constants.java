package com.blackcracks.blich.util;

public class Constants {

    /**
     * Starting from 14, because {@link com.blackcracks.blich.adapter.ScheduleAdapter} uses 0 - 13
     */
    public static final int EXAMS_LOADER_ID = 14;
    public static final int EVENTS_LOADER_ID = 15;
    public static final int SCHEDULE_LOADER_ID = 16;
    public static final int NEWS_LOADER_ID = 17;

    public class IntentConstants {

        public static final String ACTION_SYNC_CALLBACK = "sync_callback";
        public static final String ACTION_FETCH_NEWS_CALLBACK = "fetch_news_callback";

        public static final String ACTION_BLICH_NOTIFY = "blich_notify";


        public static final String EXTRA_FETCH_STATUS = "extra_fetch_status";
        public static final String EXTRA_NEWS_CATEGORY = "extra_category";

        public static final String EXTRA_ARTICLE_BODY = "article_body";
    }
}
