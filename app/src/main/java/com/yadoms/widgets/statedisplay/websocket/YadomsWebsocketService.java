package com.yadoms.widgets.statedisplay.websocket;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yadoms.widgets.statedisplay.R;
import com.yadoms.widgets.statedisplay.SettingsActivity;
import com.yadoms.widgets.statedisplay.SwitchAppWidget;
import com.yadoms.widgets.statedisplay.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_KEYWORD_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_VALUE;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.REMOTE_UPDATE_ACTION_WIDGET_ID;
import static com.yadoms.widgets.statedisplay.SwitchAppWidget.WIDGET_REMOTE_UPDATE_ACTION;
import static com.yadoms.widgets.statedisplay.widgetPrefs.PREF_PREFIX_KEY;




public class YadomsWebsocketService extends Service  {
    private static final int SUBSCRIBE_TO_KEYWORD = 1;
    private static final int CLOSE_WEB_SOCKET = 2;
    private static final int DISCONNECT_LOOPER = 3;

    private static final String KEYWORD_ID = "keywordId";

    private static final int ONGOING_NOTIFICATION_ID = 1;

    private Handler serviceHandler;
    private Looper serviceLooper;
    private WebSocket webSocket;
    private YadomsWebSocketListener yadomsWebSocketListener;
    private Set<Integer> listenKeywords = new HashSet<>();

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUBSCRIBE_TO_KEYWORD:
                    subscribeToKeyword(msg.getData().getInt(KEYWORD_ID));
                    break;
                case CLOSE_WEB_SOCKET:
                    closeWebSocket();
                    break;
                case DISCONNECT_LOOPER:
                    serviceLooper.quit();
                    break;
            }
        }
    }

    private void subscribeToKeyword(int keywordId) {
        listenKeywords.add(keywordId);

        if (!yadomsWebSocketListener.isConnected())
        {
            return;
        }

        sendKeywordFilterToYadoms();
    }

    private void sendKeywordFilterToYadoms() {
        try
        {
            JSONObject message = new JSONObject();
            message.put("type", "acquisitionFilter");
            message.put("data", new JSONArray(listenKeywords.toArray()));
            Log.d("YadomsWebsocketService", "subscribeToKeyword, " + message.toString());
            webSocket.send(message.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void connectToWebSocket(String server_url, String server_port) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("ws://" + server_url + ":" + server_port + "/ws")
                .build();
        yadomsWebSocketListener = new YadomsWebSocketListener();
        webSocket = okHttpClient.newWebSocket(request, yadomsWebSocketListener);
    }

    private void closeWebSocket() {
        if (!yadomsWebSocketListener.isConnected()) {
            return;
        }
        webSocket.close(1000, "Goodbye, World!");
    }

    public YadomsWebsocketService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, ?> allPrefs = prefs.getAll();

        connectToWebSocket(prefs.getString("server_url", null).trim(), prefs.getString("server_port", "8080").trim());

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().matches(PREF_PREFIX_KEY + "\\d+" + "keyword")) {
                listenKeywords.add((Integer)entry.getValue());
            }
        }
        if (!listenKeywords.isEmpty()) {
            sendKeywordFilterToYadoms();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setSmallIcon(R.drawable.small_icon)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);

        HandlerThread thread = new HandlerThread("WebSocket service");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        EventBus.getDefault().register(this);
    }

    public void onEvent(SubscribeToKeywordEvent subscribeToKeywordEvent)
    {
        if (yadomsWebSocketListener.isConnected()) {
            Message message = Message.obtain();
            message.what = SUBSCRIBE_TO_KEYWORD;
            Bundle data = new Bundle();
            data.putInt(KEYWORD_ID, subscribeToKeywordEvent.getKeywordId());
            message.setData(data);
            serviceHandler.sendMessage(message);
        }
    }

    public void onEvent(AcquisitionUpdateEvent acquisitionUpdateEvent)
    {
        if (yadomsWebSocketListener.isConnected()) {
            ArrayList<Integer> widgetsId = Util.findWidgetsUsingKeyword(getApplicationContext(), acquisitionUpdateEvent.getKeywordId());
            for (int widgetId : widgetsId) {
                Intent intent = new Intent(getApplicationContext(), SwitchAppWidget.class);
                intent.setAction(WIDGET_REMOTE_UPDATE_ACTION);
                intent.putExtra(REMOTE_UPDATE_ACTION_WIDGET_ID, widgetId);
                intent.putExtra(REMOTE_UPDATE_ACTION_KEYWORD_ID, acquisitionUpdateEvent.getKeywordId());
                intent.putExtra(REMOTE_UPDATE_ACTION_VALUE, acquisitionUpdateEvent.getValue());
                sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        serviceHandler.sendEmptyMessage(CLOSE_WEB_SOCKET);
        serviceHandler.sendEmptyMessage(DISCONNECT_LOOPER);
        EventBus.getDefault().unregister(this);

//        Intent broadcastIntent = new Intent(this, ServiceRestarterBroadcastReceiver.class);
//        sendBroadcast(broadcastIntent);

        super.onDestroy();
    }
}
