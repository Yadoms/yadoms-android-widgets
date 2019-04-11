package com.yadoms.widgets.statedisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.createWidgetsUpdateJob(context);
    }
}