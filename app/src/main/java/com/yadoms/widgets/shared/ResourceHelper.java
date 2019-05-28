package com.yadoms.widgets.shared;

import android.content.Context;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

public class ResourceHelper {
    @ColorInt
    public static int getColorFromResource(Context context, @ColorRes int id) {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.getColor(id);
        }
        return context.getResources().getColor(id);
    }
}
