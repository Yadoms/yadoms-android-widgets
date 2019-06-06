package com.yadoms.widgets.application;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yadoms.widgets.BuildConfig;
import com.yadoms.widgets.application.preferences.DatabaseHelper;
import com.yadoms.widgets.shared.Widget;
import com.yadoms.widgets.shared.restClient.Client;
import com.yadoms.widgets.shared.restClient.GetResponseHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ReadWidgetsStateWorker extends Worker
{
    private static final String UNIQUE_WORK_NAME = ReadWidgetsStateWorker.class.getSimpleName() + "WorkName";
    private static final int SERVER_POLL_PERIOD_SECONDS = BuildConfig.DEBUG ? 500 : 30;
    private static final int SERVER_POLL_AFTER_CONNECTION_FAILED_RETRY_SECONDS = BuildConfig.DEBUG ? 10 : 60;

    public static String WIDGET_REMOTE_UPDATE_ACTION = "WidgetRemoteUpdateAction";
    public static String REMOTE_UPDATE_ACTION_WIDGET_ID = "WidgetId";
    public static String REMOTE_UPDATE_ACTION_KEYWORD_ID = "KeywordId";
    public static String REMOTE_UPDATE_ACTION_VALUE = "Value";


    public ReadWidgetsStateWorker(@NonNull Context context,
                                  @NonNull WorkerParameters params)
    {
        super(context, params);
    }

    @Override
    public @NonNull Result doWork()
    {
        switch(readWidgets(getApplicationContext()))
        {
            case SUCCESS: {
                restart(SERVER_POLL_PERIOD_SECONDS);
                return Result.success();
            }
            case INVALID_CONFIGURATION: {
                // No restart
                return Result.failure();
            }
            case CONNECTION_FAILED: {
                restart(SERVER_POLL_AFTER_CONNECTION_FAILED_RETRY_SECONDS);
                return Result.failure();
            }
        }
        return Result.failure();
    }

    enum ReadWidgetsResult
    {
        SUCCESS,
        INVALID_CONFIGURATION,
        CONNECTION_FAILED
    }

    static private ReadWidgetsResult readWidgets(final Context context)
    {
        Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "Read widgets state...");
        try
        {
            final DatabaseHelper databaseHelper = new DatabaseHelper(context);

            Set<Integer> listenKeywords = databaseHelper.getAllKeywords();
            if (listenKeywords.isEmpty())
            {
                Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "No keyword to monitor");
                return ReadWidgetsResult.INVALID_CONFIGURATION;
            }

            Client yadomsRestClient = new Client(context);

            final boolean[] success = new boolean[1];
            for (final int keywordId : listenKeywords)
            {
                final Semaphore semaphore = new Semaphore(1);//TODO encore nécessaire ?
                semaphore.acquire();
                yadomsRestClient.getKeywordLastValue(keywordId, new GetResponseHandler()//TODO utiliser la requête getKeywordListLastData
                {
                    @Override
                    public void onSuccess(Object[] objects)
                    {
                        final String lastValue = ((String[]) objects)[0];

                        Set<Widget> widgets = databaseHelper.getWidgetsFromKeyword(keywordId);
                        for (Widget widget : widgets) {
                            if (!fireEventToWidget(context, widget, lastValue))
                            {
                                success[0] = false;
                                semaphore.release();
                                return;
                            }
                            Log.d(getClass().getSimpleName(), "Widget " + widget.id + "(keyword " + widget.keywordId + ") was updated to value " + lastValue);
                        }
                        success[0] = true;
                        semaphore.release();
                    }
                    @Override
                    public void onFailure()
                    {
                        Log.e(ReadWidgetsStateWorker.class.getSimpleName(), "Retrieve last keyword values failed");
                        success[0] = false;
                        semaphore.release();
                    }
                });
                semaphore.acquire();
                if (!success[0])
                    break;
            }
            return success[0] ? ReadWidgetsResult.SUCCESS : ReadWidgetsResult.CONNECTION_FAILED;
        }
        catch (SQLException e)
        {
            Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "Invalid configuration");
            e.printStackTrace();
            return ReadWidgetsResult.INVALID_CONFIGURATION;
        }
        catch (InvalidConfigurationException e)
        {
            Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "Invalid configuration");
            e.printStackTrace();
            return ReadWidgetsResult.INVALID_CONFIGURATION;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ReadWidgetsResult.INVALID_CONFIGURATION;//TODO
        }
    }

    private static Boolean fireEventToWidget(Context context, Widget widget, String lastValue) {
        Intent intent;
        try {
            intent = new Intent(context, Class.forName(widget.className));
        } catch (ClassNotFoundException e) {
            Log.e(ReadWidgetsStateWorker.class.getSimpleName(), "Widget class name not supported");
            return false;
        }
        intent.setAction(WIDGET_REMOTE_UPDATE_ACTION);
        intent.putExtra(REMOTE_UPDATE_ACTION_WIDGET_ID, widget.id);
        intent.putExtra(REMOTE_UPDATE_ACTION_KEYWORD_ID, widget.keywordId);
        intent.putExtra(REMOTE_UPDATE_ACTION_VALUE, lastValue);
        context.sendBroadcast(intent);
        return true;
    }

    public static void startService(boolean forceNow)
    {
        Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "Start service");
        if (isRunning() && !forceNow)
            return;

        restart(0);
    }

    static void stopService()
    {
        Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "Stop service");
        WorkManager.getInstance().cancelUniqueWork(UNIQUE_WORK_NAME);
    }

    private static void restart(int delaySeconds)
    {
        Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "Schedule next poll (" + delaySeconds + "s)");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest readWidgetsStateWorker = new OneTimeWorkRequest.Builder(ReadWidgetsStateWorker.class)
                .setConstraints(constraints)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE, readWidgetsStateWorker);
    }

    private static boolean isRunning()
    {
        List<WorkInfo> status = WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME).getValue();
        if (status == null || status.isEmpty())
            return false;

        WorkInfo.State state = status.get(0).getState();
        return state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.BLOCKED || state == WorkInfo.State.RUNNING;
    }
}

