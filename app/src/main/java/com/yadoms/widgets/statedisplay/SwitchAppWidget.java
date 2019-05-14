package com.yadoms.widgets.statedisplay;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.yadoms.widgets.statedisplay.preferences.DatabaseHelper;

import java.sql.SQLException;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SwitchAppWidgetConfigureActivity SwitchAppWidgetConfigureActivity}
 */
public class SwitchAppWidget
        extends AppWidgetProvider
{
    public static String CLICK_ON_WIDGET_ACTION = "ClickOnWidgetAction";
    public static String WIDGET_ACTION_WIDGET_ID = "WidgetId";

    public static String WIDGET_REMOTE_UPDATE_ACTION = "WidgetRemoteUpdateAction";
    public static String REMOTE_UPDATE_ACTION_WIDGET_ID = "WidgetId";
    public static String REMOTE_UPDATE_ACTION_KEYWORD_ID = "KeywordId";
    public static String REMOTE_UPDATE_ACTION_VALUE = "Value";

    private static SparseBooleanArray currentState = new SparseBooleanArray();

    private static DatabaseHelper DatabaseHelper;

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        Widget widget;
        try
        {
            widget = getDatabaseHelper(context).getWidget(appWidgetId);
        }
        catch (InvalidConfigurationException e)
        {
            Log.w(SwitchAppWidget.class.getSimpleName(), "Fail to update widget : widget not found in database");
            widget = new Widget(appWidgetId, 0, "");
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.switch_app_widget);
        views.setTextViewText(R.id.appwidget_label,
                              (widget.label != null && !widget.label.isEmpty()) ? widget.label : Integer.toString(widget.keywordId));

        views.setImageViewResource(R.id.appwidget_image,
                                   currentState.get(appWidgetId) ? R.drawable.ic_baseline_toggle_on_24px : R.drawable.ic_baseline_toggle_off_24px);

        //TODO dans le service, mettre un listener sur les préférences pour mettre à jour la liste de KW à s'inscrire
//        if (prefs.keyword != 0)
//            EventBus.getDefault().post(new SubscribeToKeywordEvent(prefs.keyword));

        Intent intent = new Intent(context, SwitchAppWidget.class);
        intent.setAction(CLICK_ON_WIDGET_ACTION);
        intent.putExtra(WIDGET_ACTION_WIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(final Context context,
                          Intent intent)
    {
        super.onReceive(context, intent);

        try
        {
            if (intent.getAction().equals(CLICK_ON_WIDGET_ACTION))
            {
                final int widgetId = intent.getIntExtra(WIDGET_ACTION_WIDGET_ID, 0);
                currentState.put(widgetId, !(currentState.get(widgetId)));

                YadomsRestClient yadomsRestClient = new YadomsRestClient(context.getApplicationContext());
                yadomsRestClient.command(getDatabaseHelper(context).getWidget(widgetId).keywordId,
                        currentState.get(widgetId),
                        new YadomsRestCommandResponseHandler(){
                            @Override
                            public void onSuccess()
                            {
                                onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
                            }
                        });
            }
            else if(intent.getAction().equals(WIDGET_REMOTE_UPDATE_ACTION))
            {
                final int widgetId = intent.getIntExtra(REMOTE_UPDATE_ACTION_WIDGET_ID, 0);
                final String value = intent.getStringExtra(REMOTE_UPDATE_ACTION_VALUE);
                currentState.put(widgetId, !value.equals("0"));
                onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
            }
        }
        catch (InvalidConfigurationException e)
        {
            Log.e(getClass().getSimpleName(), e.getMessage());
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
        DatabaseHelper databaseHelper = getDatabaseHelper(context);
        for (int appWidgetId : appWidgetIds)
        {
            try
            {
                databaseHelper.deleteWidget(appWidgetId);
            }
            catch (SQLException e)
            {
                Log.w(getClass().getSimpleName(), "Fail to delete widget #" + appWidgetId);
                e.printStackTrace();
            }
        }
    }

    private static DatabaseHelper getDatabaseHelper(Context context)
    {
        if (DatabaseHelper == null)
        {
            try {
                DatabaseHelper = new DatabaseHelper(context);
            } catch (SQLException e) {
                Toast.makeText(context,
                        context.getString(R.string.unable_to_save_preferences),
                        Toast.LENGTH_LONG).show();
            }
        }
        return DatabaseHelper;
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

