package com.yadoms.widgets.statedisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenStateReceiver extends BroadcastReceiver {

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

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            ReadWidgetsStateJobService.notifyScreenOn(context, false);
        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            ReadWidgetsStateJobService.notifyScreenOn(context, true);
        }
    }
}
