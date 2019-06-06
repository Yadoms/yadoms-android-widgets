package com.yadoms.widgets.widgets.standardSwitch;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.yadoms.widgets.R;
import com.yadoms.widgets.application.InvalidConfigurationException;
import com.yadoms.widgets.application.ReadWidgetsStateWorker;
import com.yadoms.widgets.application.preferences.DatabaseHelper;
import com.yadoms.widgets.shared.ResourceHelper;
import com.yadoms.widgets.shared.Widget;
import com.yadoms.widgets.shared.restClient.Client;
import com.yadoms.widgets.shared.restClient.CommandResponseHandler;

import java.security.InvalidParameterException;
import java.sql.SQLException;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SwitchAppWidgetConfigureActivity SwitchAppWidgetConfigureActivity}
 */
public class SwitchAppWidget
        extends AppWidgetProvider {
    private static final String START_SERVICE_EXTRA_WIDGET_IDS = "StartServiceExtraWidgetIds";
    public static String CLICK_ON_WIDGET_ACTION = "ClickOnWidgetAction";
    public static String WIDGET_ACTION_WIDGET_ID = "WidgetId";

    private static SparseBooleanArray currentState = new SparseBooleanArray();

    private static DatabaseHelper DatabaseHelper;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        //TODO code redondant avec onUpdate
        Intent intent = new Intent(context, SwitchAppWidgetUpdateService.class);
        intent.putExtra(START_SERVICE_EXTRA_WIDGET_IDS, new int[]{widgetId});
        context.startService(intent);
    }

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Intent intent = new Intent(context, SwitchAppWidgetUpdateService.class);
        intent.putExtra(START_SERVICE_EXTRA_WIDGET_IDS, appWidgetIds);
        context.startService(intent);
    }

    @Override
    public void onDeleted(Context context,
                          int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        DatabaseHelper databaseHelper = getDatabaseHelper(context);
        for (int appWidgetId : appWidgetIds) {
            try {
                databaseHelper.deleteWidget(appWidgetId);
            } catch (SQLException e) {
                Log.w(getClass().getSimpleName(), "Fail to delete widget #" + appWidgetId);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(final Context context,
                          Intent intent) {
        try {
            if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
                Log.d("TEST TODO", "A virer");

                /* TODO non testé
                int[] widgetsId = intent.getIntArrayExtra(START_SERVICE_EXTRA_WIDGET_IDS);
                if (widgetsId != null && widgetsId.length > 0)
                    onUpdate(context, AppWidgetManager.getInstance(context), widgetsId);*/

            } else if (CLICK_ON_WIDGET_ACTION.equals(intent.getAction())) {
                final int widgetId = intent.getIntExtra(WIDGET_ACTION_WIDGET_ID, 0);
                currentState.put(widgetId, !(currentState.get(widgetId)));

                final Client yadomsRestClient = new Client(context.getApplicationContext());
                final int keywordId = getDatabaseHelper(context).getWidget(widgetId).keywordId;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        yadomsRestClient.command(keywordId,
                                currentState.get(widgetId),
                                new CommandResponseHandler() {
                                    @Override
                                    public void onSuccess() {
                                        onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
                                    }
                                });
                    }
                });

            } else if (ReadWidgetsStateWorker.WIDGET_REMOTE_UPDATE_ACTION.equals(intent.getAction())) { //TODO doublon avec ACTION_APPWIDGET_UPDATE ?
                final int widgetId = intent.getIntExtra(ReadWidgetsStateWorker.REMOTE_UPDATE_ACTION_WIDGET_ID, 0);
                final String value = intent.getStringExtra(ReadWidgetsStateWorker.REMOTE_UPDATE_ACTION_VALUE);
                currentState.put(widgetId, !value.equals("0"));

                onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
            }
        } catch (InvalidConfigurationException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }

        super.onReceive(context, intent);
    }



/*TODO virer ?
    static void updateAppWidget(Context context,
                                     AppWidgetManager appWidgetManager,
                                     int appWidgetId) {
        Widget widget;
        try {
            widget = getDatabaseHelper(context).getWidget(appWidgetId);
        } catch (InvalidConfigurationException e) {
            Log.w(SwitchAppWidget.class.getSimpleName(), "Fail to update widget : widget not found in database");
            return;
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.switch_app_widget);
        views.setTextViewText(R.id.appwidget_label,
                (widget.label != null && !widget.label.isEmpty()) ? widget.label : Integer.toString(widget.keywordId));
//TODO corriger les glitchs 'Unknown' sur le label
        views.setImageViewResource(R.id.appwidget_image,
                currentState.get(appWidgetId) ? translateResourceImage(R.drawable.ic_baseline_toggle_on_24px) : translateResourceImage(R.drawable.ic_baseline_toggle_off_24px));
        views.setInt(R.id.appwidget_image, "setColorFilter", ResourceHelper.getColorFromResource(context, currentState.get(appWidgetId) ? R.color.yadomsOfficial : R.color.off));

        Intent intent = new Intent(context, SwitchAppWidget.class);
        intent.setAction(CLICK_ON_WIDGET_ACTION);
        intent.putExtra(WIDGET_ACTION_WIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }*/

    private static int translateResourceImage(int id) {
        // Translate resource image to use vector image for SDK >= 26 and mipmap for older SDK
        // Needed by widgets as ImageView before SDK 26 doesn't support vector images

        if (Build.VERSION.SDK_INT >= 26) {
            return id;
        }

        final SparseIntArray imagesResourceIdMap = new SparseIntArray();
        imagesResourceIdMap.put(R.drawable.ic_baseline_toggle_off_24px, R.mipmap.ic_baseline_toggle_off);
        imagesResourceIdMap.put(R.drawable.ic_baseline_toggle_on_24px, R.mipmap.ic_baseline_toggle_on);

        int returnedId = imagesResourceIdMap.get(id, -1);
        if (returnedId == -1)
            throw new InvalidParameterException("Image resource ID not found");
        return returnedId;
    }

    private static DatabaseHelper getDatabaseHelper(Context context) {
        if (DatabaseHelper == null) {
            try {
                DatabaseHelper = new DatabaseHelper(context);
            } catch (SQLException e) {
                Toast.makeText(context,
                        context.getString(R.string.unable_to_access_configuration),
                        Toast.LENGTH_LONG).show();
            }
        }
        return DatabaseHelper;
    }

    public static class SwitchAppWidgetUpdateService extends Service {
        int[] widgetsId;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);

            widgetsId = intent.getIntArrayExtra(START_SERVICE_EXTRA_WIDGET_IDS);

            // Update the widget
            RemoteViews remoteViews = buildRemoteView(this);

            // Push update to homescreen
            pushUpdate(remoteViews);

            // No more updates so stop the service and free resources
            stopSelf();
            return flags;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public RemoteViews buildRemoteView(Context context) {
            int appWidgetId = widgetsId[0]; // TODO gérer tout le tableau

            Widget widget;
            try {
                widget = getDatabaseHelper(context).getWidget(appWidgetId);
            } catch (InvalidConfigurationException e) {
                Log.w(SwitchAppWidget.class.getSimpleName(), "Fail to update widget : widget not found in database");
                return null;
            }

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.switch_app_widget);
            views.setTextViewText(R.id.appwidget_label,
                    (widget.label != null && !widget.label.isEmpty()) ? widget.label : Integer.toString(widget.keywordId));
//TODO corriger les glitchs 'Unknown' sur le label
            views.setImageViewResource(R.id.appwidget_image,
                    currentState.get(appWidgetId) ? translateResourceImage(R.drawable.ic_baseline_toggle_on_24px) : translateResourceImage(R.drawable.ic_baseline_toggle_off_24px));
            views.setInt(R.id.appwidget_image, "setColorFilter", ResourceHelper.getColorFromResource(context, currentState.get(appWidgetId) ? R.color.yadomsOfficial : R.color.off));

            Intent intent = new Intent(context, SwitchAppWidget.class);
            intent.setAction(CLICK_ON_WIDGET_ACTION);
            intent.putExtra(WIDGET_ACTION_WIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);


            return views;
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) //TODO utile (normalement pour gérer la rotation) ?
        {/*TODO remettre ?
            int oldOrientation = this.getResources().getConfiguration().orientation;

            if(newConfig.orientation != oldOrientation)
            {
                // Update the widget
                RemoteViews remoteViews = buildRemoteView(this);

                // Push update to homescreen
                pushUpdate(remoteViews);
            }*/
        }

        private void pushUpdate(RemoteViews remoteViews) {
            ComponentName myWidget = new ComponentName(this, SwitchAppWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(myWidget, remoteViews);
        }
    }
}

