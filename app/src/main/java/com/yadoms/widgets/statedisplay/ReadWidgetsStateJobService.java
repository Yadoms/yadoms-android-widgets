package com.yadoms.widgets.statedisplay;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yadoms.widgets.statedisplay.preferences.DatabaseHelper;

import java.sql.SQLException;
import java.util.Set;

import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_KEYWORD_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_VALUE;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_WIDGET_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.WIDGET_REMOTE_UPDATE_ACTION;

public class ReadWidgetsStateJobService extends JobService {

    static private boolean isScreenOn = true;

    static void notifyScreenOn(Context context, boolean on) {
        isScreenOn = on;
        start(context);
    }

    static void start(Context context) {

        if (!isScreenOn) {
            Log.d("UpdateWidgetsService", "Screen is OFF, service stopped");
            return;
        }

        Log.d("UpdateWidgetsService", "Service started");
        ComponentName serviceComponent = new ComponentName(context, ReadWidgetsStateJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent)
                .setMinimumLatency(1000)
                .setOverrideDeadline(3000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setPersisted(true);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        try
        {
            final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

            Set<Integer> listenKeywords = databaseHelper.getAllKeywords();
            if (listenKeywords.isEmpty()) {
                Log.d("UpdateWidgetsService", "No keyword to monitor. Service stopped.");
                return false;
            }

            YadomsRestClient yadomsRestClient = new YadomsRestClient(getApplicationContext());

            for (final int keywordId : listenKeywords) {
                yadomsRestClient.getKeywordLastValue(keywordId, new YadomsRestGetResponseHandler(){
                    @Override
                    void onSuccess(Object[] objects)
                    {
                        final String lastValue = ((String[]) objects)[0];

                        Set<Integer> widgetsId = databaseHelper.getWidgetsFromKeyword(keywordId);
                        for (int widgetId : widgetsId) {
                            Intent intent = new Intent(getApplicationContext(), SwitchAppWidget.class);
                            intent.setAction(WIDGET_REMOTE_UPDATE_ACTION);
                            intent.putExtra(REMOTE_UPDATE_ACTION_WIDGET_ID, widgetId);
                            intent.putExtra(REMOTE_UPDATE_ACTION_KEYWORD_ID, keywordId);
                            intent.putExtra(REMOTE_UPDATE_ACTION_VALUE, lastValue);
                            sendBroadcast(intent);
                            Log.d("UpdateWidgetsService", "Widget " + widgetId + "(keyword " + keywordId + ") was updated to value " + lastValue);
                        }
                    }
                    @Override
                    void onFailure()
                    {
                    }
                });
            }
            start(getApplicationContext());
            return true;
        }
        catch (SQLException e)
        {
            Log.d(getClass().getSimpleName(), "Invalid configuration. Service stopped.");
            e.printStackTrace();
            return false;
        }
        catch (InvalidConfigurationException e)
        {
            Log.d(getClass().getSimpleName(), "Invalid configuration. Service stopped.");
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
