package com.yadoms.widgets.application;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import com.yadoms.widgets.application.preferences.DatabaseHelper;
import com.yadoms.widgets.widgets.standardSwitch.SwitchAppWidget;

public class WidgetsService {
    public static void refreshAll(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SwitchAppWidget.updateAppWidget(context, appWidgetManager, DatabaseHelper.getAllWidgetIds(context));
    }

    static void onSreenOn(Context context)
    {
        // Manage
        Log.d(WidgetsService.class.getSimpleName(), "onSreenOn");
        refreshAll(context);
    }

    static void onSreenOff(Context context)
    {
        Log.d(WidgetsService.class.getSimpleName(), "onSreenOff");
        refreshAll(context);
    }
}
