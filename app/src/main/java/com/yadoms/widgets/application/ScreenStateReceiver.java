package com.yadoms.widgets.application;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver {

    public static void start(Context applicationContext) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        BroadcastReceiver receiver = new ScreenStateReceiver();
        applicationContext.registerReceiver(receiver, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(), "Receive " + intent.getAction());
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            // If keyguard is active, ACTION_USER_PRESENT will be sent after user unlock device
            // If not active, ACTION_USER_PRESENT will never be sent
            // So wait ACTION_USER_PRESENT event only if keyguard active
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null && km.isKeyguardLocked())
                return;
            WidgetsService.onSreenOn(context);
        }
        else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            WidgetsService.onSreenOn(context);
        }
        else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
            WidgetsService.onSreenOff(context);
        }
    }
}
