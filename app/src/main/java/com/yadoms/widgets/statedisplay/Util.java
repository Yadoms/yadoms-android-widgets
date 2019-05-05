package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yadoms.widgets.statedisplay.widgetPrefs.PREF_PREFIX_KEY;

class Util {

    static ArrayList<Integer> findWidgetsUsingKeyword(Context context, int keywordId) {
        ArrayList<Integer> widgets = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, ?> allPrefs = prefs.getAll();
        Pattern pattern = Pattern.compile(PREF_PREFIX_KEY + "(\\d+)" + "_keyword");
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.find()) {
                int widgetId = Integer.parseInt(matcher.group(1));
                if ((Integer)allPrefs.get(matcher.group(0)) == keywordId)
                    widgets.add(widgetId);
            }
        }

        return widgets;
    }
}
