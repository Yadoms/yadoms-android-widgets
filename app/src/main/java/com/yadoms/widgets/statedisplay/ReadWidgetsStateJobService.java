package com.yadoms.widgets.statedisplay;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_KEYWORD_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_VALUE;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_WIDGET_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.WIDGET_REMOTE_UPDATE_ACTION;
import static com.yadoms.widgets.statedisplay.widgetPrefs.PREF_PREFIX_KEY;

public class ReadWidgetsStateJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        try {
            YadomsRestClient yadomsRestClient = new YadomsRestClient(getApplicationContext());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Map<String, ?> allPrefs = prefs.getAll();

            Set<Integer> listenKeywords = new HashSet<>();
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                if (entry.getKey().matches(PREF_PREFIX_KEY + "\\d+" + "_keyword")) {
                    listenKeywords.add((Integer)entry.getValue());
                }
            }
            if (listenKeywords.isEmpty()) {
                Log.d("UpdateWidgetsService", "No more keyword to monitor. Service stopped.");
                return false;
            }

            for (final int keywordId : listenKeywords) {
                yadomsRestClient.getKeywordLastValue(keywordId, new YadomsRestGetResponseHandler(){
                    @Override
                    void onSuccess(Object[] objects)
                    {
                        final String lastValue = ((String[]) objects)[0];

                        ArrayList<Integer> widgetsId = Util.findWidgetsUsingKeyword(getApplicationContext(), keywordId);
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
            Util.createWidgetsUpdateJob(getApplicationContext());
            return true;
        }
        catch (InvalidConfigurationException e)
        {
            Log.d("UpdateWidgetsService", "Invalid configuration. Service stopped.");
            return false;
        }
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
