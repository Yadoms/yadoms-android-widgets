package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class widgetPref
{
    static final String PREF_PREFIX_KEY = "appwidget_";

    private final Context context;
    private final int appWidgetId;

    Integer keyword;
    String label;

    widgetPref(Context context,
               int appWidgetId)
    {
        this.context = context;
        this.appWidgetId = appWidgetId;
        load();
    }

    private void load()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        keyword = prefs.getInt(PREF_PREFIX_KEY + appWidgetId + "keyword", 0);
        label = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "label", null);
    }

    void save()
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId + "keyword", keyword);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "label", label);
        prefs.apply();
    }

    void delete()
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "keyword");
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "label");
        prefs.apply();
    }
}