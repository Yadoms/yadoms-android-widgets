package com.yadoms.widgets.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver {

    public static void start(Context applicationContext) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        BroadcastReceiver receiver = new ScreenStateReceiver();
        applicationContext.registerReceiver(receiver, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            Log.d(ScreenStateReceiver.class.getSimpleName(), "Screen is ON and user is present");
            //TODO remettre ReadWidgetsStateWorker.startService(false);
            WidgetsService.refreshAll(context);
        }
    }
}
