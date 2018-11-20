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
