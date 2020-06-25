package com.yadoms.widgets.application;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import com.yadoms.widgets.application.preferences.DatabaseHelper;
import com.yadoms.widgets.widgets.standardSwitch.SwitchAppWidget;

import java.sql.SQLException;

public class WidgetsService {
    public static void refreshAll(Context context) {
        int[] appWidgetIds;
        try {
            final DatabaseHelper databaseHelper = new DatabaseHelper(context);
            appWidgetIds = databaseHelper.getAllWidgetIds();
        } catch (SQLException e) {
            Log.d(WidgetsService.class.getSimpleName(), "Invalid configuration");
            e.printStackTrace();
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SwitchAppWidget.updateAppWidget(context, appWidgetManager, appWidgetIds);
    }
}
