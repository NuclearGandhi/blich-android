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

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.PreferenceUtils;

/**
 * A data class holding information about exams.
 */
public class RawExam extends RawModifier {

    public RawExam() {}

    @Override
    public int getColor() {
        return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_exam_key);
    }

    public String getBaseTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (title.contains("מבחן") ||
                title.contains("בוחן") ||
                title.contains("מבחני") ||
                title.contains("מתכונת") ||
                title.contains("בגרות")) {
            this.title = title;
        } else {
            this.title = "מבחן ב" + title;
        }
    }
}
