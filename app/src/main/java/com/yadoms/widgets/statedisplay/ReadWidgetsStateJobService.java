package com.yadoms.widgets.statedisplay;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
        YadomsRestClient yadomsRestClient = new YadomsRestClient(getApplicationContext());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, ?> allPrefs = prefs.getAll();

        Set<Integer> listenKeywords = new HashSet<>();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().matches(PREF_PREFIX_KEY + "\\d+" + "keyword")) {
                listenKeywords.add((Integer)entry.getValue());
            }
        }
        if (listenKeywords.isEmpty()) {
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
                    }
                }
                @Override
                void onFailure()
                {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.unknown_error),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        Util.createWidgetsUpdateJob(getApplicationContext());
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
