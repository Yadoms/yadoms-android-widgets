package com.yadoms.widgets.statedisplay;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link StateDisplayAppWidgetConfigureActivity StateDisplayAppWidgetConfigureActivity}
 */
public class StateDisplayAppWidget
        extends AppWidgetProvider
{

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {

        CharSequence widgetText = StateDisplayAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.state_display_app_widget);
        views.setTextViewText(R.id.yadoms_server_address, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context,
                          int[] appWidgetIds)
    {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds)
        {
            StateDisplayAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }
}

