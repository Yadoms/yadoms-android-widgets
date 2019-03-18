package com.yadoms.widgets.statedisplay;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.RemoteViews;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SwitchAppWidgetConfigureActivity SwitchAppWidgetConfigureActivity}
 */
public class SwitchAppWidget
        extends AppWidgetProvider
{
    public static String CLICK_ON_WIDGET_ACTION = "ClickOnWidgetAction";
    public static String WIDGET_ACTION_WIDGET_ID = "WidgetId";

    private static SparseBooleanArray currentState = new SparseBooleanArray();

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        widgetPref prefs = new widgetPref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.switch_app_widget);
        views.setTextViewText(R.id.appwidget_label, !prefs.label.isEmpty() ? prefs.label : prefs.keyword);

        views.setImageViewResource(R.id.appwidget_image,
                                   currentState.get(appWidgetId) ? R.drawable.ic_baseline_toggle_on_24px : R.drawable.ic_baseline_toggle_off_24px);

        Log.d("updateAppWidget", "prefs.keyword = " + prefs.keyword);

        Intent intent = new Intent(context, SwitchAppWidget.class);
        intent.setAction(CLICK_ON_WIDGET_ACTION);
        intent.putExtra(WIDGET_ACTION_WIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context,
                          Intent intent)
    {
        super.onReceive(context, intent);

        if (intent.getAction().equals(CLICK_ON_WIDGET_ACTION))
        {
            int widgetId = intent.getIntExtra(WIDGET_ACTION_WIDGET_ID, 0);
            currentState.put(widgetId, !(currentState.get(widgetId)));

//TODO virer ?
//            restService.sendYadomsRequest(context,
//                    new widgetPref(context, widgetId).keyword,
//                    currentState.get(widgetId));

            YadomsRestClient yadomsRestClient = new YadomsRestClient(context.getApplicationContext());
            yadomsRestClient.post("/rest/device/keyword/" + new widgetPref(context,
                                                                           widgetId).keyword.trim() + "/command",
                                  new SingleStringRequestParam(currentState.get(widgetId)),
                                  new JsonHttpResponseHandler()
                                  {
                                      @Override
                                      public void onSuccess(int statusCode,
                                                            Header[] headers,
                                                            JSONObject response)
                                      {
                                          Log.d("yadomsRestClient", "onSuccess, statusCode = " + statusCode);
                                          //TODO
                                      }

                                      @Override
                                      public void onFailure(int statusCode,
                                                            Header[] headers,
                                                            Throwable error,
                                                            JSONObject errorResponse)
                                      {
                                          Log.e("yadomsRestClient", "onFailure, statusCode = " + statusCode);
                                          //TODO
                                      }
                                  });
        }
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
            widgetPref prefs = new widgetPref(context, appWidgetId);
            prefs.delete();
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

