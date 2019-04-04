package com.yadoms.widgets.statedisplay.websocket;

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

import com.yadoms.widgets.statedisplay.SwitchAppWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final int CONNECT_TO_WEB_SOCKET = 1;
    private static final int SUBSCRIBE_TO_KEYWORD = 2;
    private static final int CLOSE_WEB_SOCKET = 3;
    private static final int DISCONNECT_LOOPER = 4;

    private static final String KEYWORD_ID = "keywordId";

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
                case CONNECT_TO_WEB_SOCKET:
                    connectToWebSocket();
                    break;
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

    private void connectToWebSocket() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("ws://10.0.2.2:8080/ws")
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
        HandlerThread thread = new HandlerThread("WebSocket service");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        serviceHandler.sendEmptyMessage(CONNECT_TO_WEB_SOCKET);

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
            ArrayList<Integer> widgetsId = findWidgetsUsingKeyword(acquisitionUpdateEvent.getKeywordId());
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

    private ArrayList<Integer> findWidgetsUsingKeyword(int keywordId) {
        ArrayList<Integer> widgets = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, ?> allPrefs = prefs.getAll();
        Pattern pattern = Pattern.compile(PREF_PREFIX_KEY + "(\\d+)" + "keyword");
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.find()) {
                widgets.add(Integer.parseInt(matcher.group(1)));
            }
        }

        return widgets;
    }


    @Override
    public void onDestroy()
    {
        serviceHandler.sendEmptyMessage(CLOSE_WEB_SOCKET);
        serviceHandler.sendEmptyMessage(DISCONNECT_LOOPER);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
