package com.yadoms.widgets.widgets.standardSwitch;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
import com.yadoms.widgets.application.preferences.DatabaseHelper;
import com.yadoms.widgets.shared.ResourceHelper;
import com.yadoms.widgets.shared.Widget;
import com.yadoms.widgets.shared.restClient.Client;
import com.yadoms.widgets.shared.restClient.CommandResponseHandler;
import com.yadoms.widgets.shared.restClient.GetResponseHandler;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SwitchAppWidgetConfigureActivity SwitchAppWidgetConfigureActivity}
 */
public class SwitchAppWidget
        extends AppWidgetProvider {
    private static final String START_SERVICE_EXTRA_WIDGET_IDS = "StartServiceExtraWidgetIds";
    public static String CLICK_ON_WIDGET_ACTION = "ClickOnWidgetAction";
    public static String WIDGET_ACTION_WIDGET_ID = "WidgetId";
    public static String WIDGET_REMOTE_UPDATE_ACTION = "WidgetRemoteUpdateAction";
    public static String REMOTE_UPDATE_ACTION_WIDGET_ID = "RemoteUpdateActionWidgetId";
    public static String REMOTE_UPDATE_ACTION_VALUE = "RemoteUpdateActionValue";

    private static SparseBooleanArray currentState = new SparseBooleanArray();

    private static DatabaseHelper DatabaseHelper;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(SwitchAppWidget.class.getSimpleName(), "updateAppWidget, appWidgetId=" + appWidgetId);
        Intent intent = new Intent(context, SwitchAppWidgetUpdateService.class);
        intent.putExtra(START_SERVICE_EXTRA_WIDGET_IDS, new int[]{appWidgetId});
        context.startService(intent);
    }

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

    public RemoteViews buildRemoteView(Context context, int widgetId, boolean newValue) {
        Log.d(getClass().getSimpleName(), "buildRemoteView, widgetId=" + widgetId + ", newValue=" + newValue);
        Widget widget;
        try {
            widget = getDatabaseHelper(context).getWidget(widgetId);
        } catch (InvalidConfigurationException e) {
            Log.w(getClass().getSimpleName(), "Fail to update widget : widget not found in database");
            throw new RuntimeException("Fail to update widget : widget not found in database");
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.switch_app_widget);
        views.setTextViewText(R.id.appwidget_label,
                (widget.label != null && !widget.label.isEmpty()) ? widget.label : Integer.toString(widget.keywordId));
        views.setImageViewResource(R.id.appwidget_image,
                newValue ? translateResourceImage(R.drawable.ic_baseline_toggle_on_24px) : translateResourceImage(R.drawable.ic_baseline_toggle_off_24px));
        views.setInt(R.id.appwidget_image, "setColorFilter", ResourceHelper.getColorFromResource(context, newValue ? R.color.yadomsOfficial : R.color.off));

        Intent intent = new Intent(context, SwitchAppWidget.class);
        intent.setAction(CLICK_ON_WIDGET_ACTION);
        intent.putExtra(WIDGET_ACTION_WIDGET_ID, widget.id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widget.id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        return views;
    }

    private void pushUpdate(Context context, int widgetId, RemoteViews remoteViews) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(getClass().getSimpleName(), "onUpdate, context=" + context + ", appWidgetIds=" + Arrays.toString(appWidgetIds));
        // Update the widgets via the service
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
            if (CLICK_ON_WIDGET_ACTION.equals(intent.getAction())) {
                final int widgetId = intent.getIntExtra(WIDGET_ACTION_WIDGET_ID, 0);
                boolean newValue = !currentState.get(widgetId);

                Log.d(getClass().getSimpleName(), "onReceive, Click, widgetId=" + widgetId + ", newValue=" + newValue);

                currentState.put(widgetId, newValue);

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

            } else if (WIDGET_REMOTE_UPDATE_ACTION.equals(intent.getAction())) {
                final int widgetId = intent.getIntExtra(REMOTE_UPDATE_ACTION_WIDGET_ID, 0);
                final String value = intent.getStringExtra(REMOTE_UPDATE_ACTION_VALUE);

                boolean newValue = !value.equals("0");
                currentState.put(widgetId, newValue);

                RemoteViews remoteViews = buildRemoteView(context, widgetId, newValue);
                pushUpdate(context, widgetId, remoteViews);
            }
            /* else if (ReadWidgetsStateWorker.WIDGET_REMOTE_UPDATE_ACTION.equals(intent.getAction())) { //TODO doublon avec ACTION_APPWIDGET_UPDATE ?
                final int widgetId = intent.getIntExtra(ReadWidgetsStateWorker.REMOTE_UPDATE_ACTION_WIDGET_ID, 0);
                final String value = intent.getStringExtra(ReadWidgetsStateWorker.REMOTE_UPDATE_ACTION_VALUE);
                currentState.put(widgetId, !value.equals("0"));

                onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
            }*/
        } catch (InvalidConfigurationException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }

        super.onReceive(context, intent);
    }

    public static class SwitchAppWidgetUpdateService extends IntentService {
        public SwitchAppWidgetUpdateService() {
            super(SwitchAppWidgetUpdateService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            if (intent == null)
                return;

            int[] widgetsId = intent.getIntArrayExtra(START_SERVICE_EXTRA_WIDGET_IDS);
            requestWidgetsState(this.getApplicationContext(), widgetsId);
        }

        private void requestWidgetsState(final Context context, int[] widgetsId) {
            Log.d(getClass().getSimpleName(), "requestWidgetsState, widgetsId=" + Arrays.toString(widgetsId));
            try {
                final DatabaseHelper databaseHelper = new DatabaseHelper(context);
                Client yadomsRestClient = new Client(context);

                // TODO optimiser en utilisant databaseHelper.getKeywords(widgetsId); et la requête getKeywordListLastData ?

                for (final int widgetId : widgetsId) {
                    final Widget widget = databaseHelper.getWidget(widgetId);

                    yadomsRestClient.getKeywordLastValue(widget.keywordId, new GetResponseHandler() {
                        @Override
                        public void onSuccess(Object[] objects) {
                            final String lastValue = ((String[]) objects)[0];
                            Log.d(getClass().getSimpleName(), "Widget " + widget.id + "(keyword " + widget.keywordId + ") was updated to value " + lastValue);
                            sendToWidget(context, widget, lastValue);
                        }

                        @Override
                        public void onFailure() {
                            Log.e(getClass().getSimpleName(), "Retrieve last keyword value failed for keyword ID " + widget.keywordId + " (widget " + widget.id + ")");
                        }
                    });
                }

            } catch (SQLException e) {
                Log.d(getClass().getSimpleName(), "Invalid configuration");
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                Log.d(getClass().getSimpleName(), "Invalid configuration");
                e.printStackTrace();
            }
        }

        private void sendToWidget(Context context, Widget widget, String value) {
            Log.d(getClass().getSimpleName(), "sendToWidget, widgetsId=" + widget.id + ", value=" + value);
            Intent intent;
            try {
                intent = new Intent(context, Class.forName(widget.className));
            } catch (ClassNotFoundException e) {
                Log.e(getClass().getSimpleName(), "Widget class name not supported");
                throw new RuntimeException("Widget class name not supported");
            }
            intent.setAction(WIDGET_REMOTE_UPDATE_ACTION);
            intent.putExtra(REMOTE_UPDATE_ACTION_WIDGET_ID, widget.id);
            intent.putExtra(REMOTE_UPDATE_ACTION_VALUE, value);
            context.sendBroadcast(intent);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
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


    }
}

