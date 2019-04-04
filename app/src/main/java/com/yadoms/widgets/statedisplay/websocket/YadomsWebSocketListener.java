package com.yadoms.widgets.statedisplay.websocket;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class YadomsWebSocketListener extends WebSocketListener {
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
        try {
            JSONObject message = new JSONObject(text);
            if (message.getString("type").equals("AcquisitionUpdate")) {
                JSONObject data = message.getJSONObject("data");
                EventBus.getDefault().post(new AcquisitionUpdateEvent(
                        data.getInt("keywordId"),
                        data.getString("value")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket,
                          ByteString bytes)
    {
        Log.d("YadomsWebSocketListener", "onMessage " + bytes.toString());
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
