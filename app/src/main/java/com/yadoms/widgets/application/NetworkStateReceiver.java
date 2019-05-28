package com.yadoms.widgets.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static boolean networkAvailable = true;//TODO initialiser en fonction de l'état réel du réseau

    public static boolean networkIsAvailable() {
        return networkAvailable;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (!wifi.isAvailable() && !mobile.isAvailable()) {
            Log.d(getClass().getSimpleName(), "Network Lost");
            networkAvailable = false;
            ReadWidgetsStateWorker.stopService();
            return;
        }

        Log.d(getClass().getSimpleName(), "Network Available");
        networkAvailable = true;
        ReadWidgetsStateWorker.startService();
    }
}
