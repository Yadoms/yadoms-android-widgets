package com.yadoms.widgets.statedisplay;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

class MessageReceivedEvent
{
    private String content;

    MessageReceivedEvent(String content) {

        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

class SendMessageEvent
{
    private String content;

    SendMessageEvent(String content) {

        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

class YadomsWebSocketListener extends WebSocketListener
{
    private boolean connected;

    @Override
    public void onOpen(WebSocket webSocket,
                       Response response)
    {
        connected = true;
    }

    @Override
    public void onMessage(WebSocket webSocket,
                          String text)
    {
        Log.d("YadomsWebSocketListener", "onMessage " + text);
        EventBus.getDefault().post(new MessageReceivedEvent(text));
    }

    @Override
    public void onClosed(WebSocket webSocket,
                         int code,
                         String reason)
    {
        connected = false;
        Log.d("YadomsWebSocketListener", "Websocket is closed " + code + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket,
                          Throwable t,
                          @Nullable Response response)
    {
        connected = false;
        Log.d("YadomsWebSocketListener", "Websocket is closed " + t.getMessage());
    }

    boolean isConnected() {
        return connected;
    }
}

public class YadomsWebsocketService extends Service  {
    private static final int CONNECT_TO_WEB_SOCKET = 1;
    private static final int SEND_MESSAGE = 2;
    private static final int CLOSE_WEB_SOCKET = 3;
    private static final int DISCONNECT_LOOPER = 4;

    private static final String KEY_MESSAGE = "keyMessage";

    private Handler serviceHandler;
    private Looper serviceLooper;
    private WebSocket webSocket;
    private YadomsWebSocketListener yadomsWebSocketListener;

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
                case SEND_MESSAGE:
                    sendMessageThroughWebSocket(msg.getData().getString(KEY_MESSAGE));
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

    private void sendMessageThroughWebSocket(String message) {
//        if (!connected) {
////            return;
////        }
////        try {
////            webSocket.sendMessage(WebSocket.PayloadType.TEXT, new Buffer().write(message.getBytes()));
////        } catch (IOException e) {
////            Timber.d("Error sending message", e);
////        }
    }

    private void connectToWebSocket() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
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
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("WebSocket service");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        serviceHandler.sendEmptyMessage(CONNECT_TO_WEB_SOCKET);

        EventBus.getDefault().register(this);
    }

    public void onEvent(SendMessageEvent sendMessageEvent) {//TODO utile ?
        if (yadomsWebSocketListener.isConnected()) {
            Message message = Message.obtain();
            message.what = SEND_MESSAGE;
            Bundle data = new Bundle();
            data.putString(KEY_MESSAGE, sendMessageEvent.getContent());
            message.setData(data);
            serviceHandler.sendMessage(message);
        }
    }


    @Override
    public void onDestroy() {
        serviceHandler.sendEmptyMessage(CLOSE_WEB_SOCKET);
        serviceHandler.sendEmptyMessage(DISCONNECT_LOOPER);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
