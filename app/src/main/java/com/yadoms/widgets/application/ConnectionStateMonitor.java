package com.yadoms.widgets.application;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    private final NetworkRequest networkRequest;
    private Context context;

    public ConnectionStateMonitor() {
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();
    }

    public void enable(Context context) {
        this.context = context;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(networkRequest, this);
        }
    }

    public void disable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(this);
        }
    }


    @Override
    public void onAvailable(Network network) {
        Log.d(getClass().getSimpleName(), "Network Available = "+ network);
        WidgetsService.refreshAll(context);
    }
}
