/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

public class Constants {

    public static class Database {
        public static final String JSON_ARRAY_HOURS = "Schedule";
        public static final String JSON_INT_CLASS_ID = "ClassId";

        public static final String JSON_INT_DAY = "Day";
        public static final String JSON_INT_HOUR = "Hour";
        public static final String JSON_ARRAY_LESSONS = "Lessons";

        public static final String JSON_STRING_SUBJECT = "Subject";
        public static final String JSON_STRING_TEACHER = "Teacher";
        public static final String JSON_STRING_ROOM = "Room";
        public static final String JSON_STRING_DATE = "Date";

        public static final String JSON_ARRAY_CHANGES = "Changes";
        public static final String JSON_OBJECT_STUDY_GROUP = "StudyGroup";
        public static final String JSON_STRING_CHANGE_TYPE = "ChangeType";
        public static final String JSON_INT_NEW_HOUR = "NewHour";
        public static final String JSON_STRING_NEW_TEACHER = "NewTeacher";
        public static final String JSON_STRING_NEW_ROOM = "NewRoom";

        public static final String JSON_ARRAY_EVENTS = "Events";
        public static final String JSON_NAME = "Name";
        public static final String JSON_INT_BEGIN_HOUR = "FromHour";
        public static final String JSON_INT_END_HOUR = "ToHour";

        public static final String JSON_ARRAY_EXAMS = "Exams";

        public static final String JSON_ARRAY_CLASSES = "Classes";
        public static final String JSON_INT_ID = "Id";
        public static final String JSON_STRING_NAME = "Name";
        public static final String JSON_INT_GRADE = "Grade";
        public static final String JSON_INT_NUMBER = "Number";

        public static final String TYPE_NEW_TEACHER = "NewTeacher";
        public static final String TYPE_NEW_HOUR = "HourMove";
        public static final String TYPE_NEW_ROOM = "NewRoom";
        public static final String TYPE_EXAM = "RawExam";
        public static final String TYPE_CANCELED = "FreeLesson";
        public static final String TYPE_EVENT = "Event";
    }
}
