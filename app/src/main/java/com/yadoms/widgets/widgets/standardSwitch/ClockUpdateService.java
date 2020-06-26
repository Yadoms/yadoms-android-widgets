package com.yadoms.widgets.widgets.standardSwitch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.yadoms.widgets.application.WidgetsService;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SwitchAppWidgetConfigureActivity SwitchAppWidgetConfigureActivity}
 */
public class ClockUpdateService
        extends Service
{
    private final static IntentFilter intentFilter;

    static
    {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
    }

    public static void start(Context context)
    {
        Intent intent = new Intent(context, ClockUpdateService.class);
        intent.setPackage(context.getPackageName());//TODO utile ?
        context.startService(intent);
    }

    public static void stop(Context context)
    {
        Intent intent = new Intent(context, ClockUpdateService.class);
        intent.setPackage(context.getPackageName());//TODO utile ?
        context.stopService(intent);
    }

    private final BroadcastReceiver clockChangedReceiver = new
            BroadcastReceiver()
            {
                public void onReceive(Context context,
                                      Intent intent)
                {
                    // Called every minute
                    Log.d(getClass().getSimpleName(), "onReceive");
                    WidgetsService.refreshAll(context);
                }
            };

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        registerReceiver(clockChangedReceiver, intentFilter);
    }

    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(clockChangedReceiver);
    }
}
