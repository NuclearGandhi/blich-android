package com.blackcracks.blich.util;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.widget.NumberPicker;

import com.afollestad.appthemeengine.Config;

import java.lang.reflect.Field;

public class ThemeUtils {
    public static void themeNumberPicker(NumberPicker picker) {
        String ateKey = Utilities.getATEKey(picker.getContext());
        Context context = picker.getContext();
        setSeparatorColor(picker, Config.accentColor(context, ateKey));
    }

    private static void setSeparatorColor(NumberPicker picker, int separatorColor) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, new ColorDrawable(separatorColor));
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
