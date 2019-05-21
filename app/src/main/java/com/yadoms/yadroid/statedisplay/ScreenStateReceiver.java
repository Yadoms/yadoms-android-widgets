package com.yadoms.yadroid.statedisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver {

    private static boolean userIsPresent;

    public static void start(Context applicationContext) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver receiver = new ScreenStateReceiver();
        applicationContext.registerReceiver(receiver, filter);

        filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        receiver = new ScreenStateReceiver();
        applicationContext.registerReceiver(receiver, filter);
    }

    public static boolean userIsPresent() {
        return userIsPresent;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction() == null)
            return;

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Log.d(ScreenStateReceiver.class.getSimpleName(), "Screen is OFF");
            userIsPresent = false;
            ReadWidgetsStateWorker.stopService();
        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            Log.d(ScreenStateReceiver.class.getSimpleName(), "Screen is ON and user is present");
            userIsPresent = true;
            ReadWidgetsStateWorker.startService();
        }
    }
}
