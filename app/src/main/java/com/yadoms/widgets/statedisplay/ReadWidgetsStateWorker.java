package com.yadoms.widgets.statedisplay;

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

import com.yadoms.widgets.statedisplay.preferences.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_KEYWORD_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_VALUE;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_WIDGET_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.WIDGET_REMOTE_UPDATE_ACTION;

public class ReadWidgetsStateWorker extends Worker
{
    private static final String UNIQUE_WORK_NAME = ReadWidgetsStateWorker.class.getSimpleName() + "WorkName";
    private static final int SERVER_POLL_PERIOD_SECONDS = 10;
    private static final int SERVER_POLL_AFTER_CONNECTION_FAILED_RETRY_SECONDS = 60;

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

            YadomsRestClient yadomsRestClient = new YadomsRestClient(context);

            final boolean[] success = new boolean[1];
            for (final int keywordId : listenKeywords)
            {
                final Semaphore semaphore = new Semaphore(1);
                semaphore.acquire();
                yadomsRestClient.getKeywordLastValue(keywordId, new YadomsRestGetResponseHandler()
                {
                    @Override
                    void onSuccess(Object[] objects)
                    {
                        final String lastValue = ((String[]) objects)[0];

                        Set<Integer> widgetsId = databaseHelper.getWidgetsFromKeyword(keywordId);
                        for (int widgetId : widgetsId) {
                            Intent intent = new Intent(context, SwitchAppWidget.class);
                            intent.setAction(WIDGET_REMOTE_UPDATE_ACTION);
                            intent.putExtra(REMOTE_UPDATE_ACTION_WIDGET_ID, widgetId);
                            intent.putExtra(REMOTE_UPDATE_ACTION_KEYWORD_ID, keywordId);
                            intent.putExtra(REMOTE_UPDATE_ACTION_VALUE, lastValue);
                            context.sendBroadcast(intent);
                            Log.d(getClass().getSimpleName(), "Widget " + widgetId + "(keyword " + keywordId + ") was updated to value " + lastValue);
                        }
                        success[0] = true;
                        semaphore.release();
                    }
                    @Override
                    void onFailure()
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

    static void startService()
    {
        Log.d(ReadWidgetsStateWorker.class.getSimpleName(), "Start service");
        if (isRunning())
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
