package com.yadoms.widgets.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            WidgetsService.refreshAll(context);
        }
    }
}
