package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.net.HttpURLConnection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

public class YadomsRestClient
{
    private final String baseUrl;
    private final Context context;

    YadomsRestClient(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String yadomsServerAddress = prefs.getString("server_url", null);
        String yadomsServerPort = prefs.getString("server_port", "8080");
        baseUrl = "http://" + yadomsServerAddress.trim() + ":" + yadomsServerPort.trim();

        boolean yadomsBasicAuthenticationEnable = prefs.getBoolean("basic_authentication", false);
        String basicAuthenticationUsername = prefs.getString("basic_authentication_username", null);
        String basicAuthenticationPassword = prefs.getString("basic_authentication_password", null);

        if (yadomsBasicAuthenticationEnable)
        {
            client.setBasicAuth(basicAuthenticationUsername, basicAuthenticationPassword);
        }
        this.context = context;
    }

    private AsyncHttpClient client = new AsyncHttpClient();

    private void get(String url,
                    String params,
                    AsyncHttpResponseHandler responseHandler)
    {
        Log.d("YadomsRestClient", "GET : " + url + ", params : " + (params != null ? params : ""));
        client.get(context,
                   getAbsoluteUrl(url),
                   new StringEntity(params != null ? params : "", ContentType.APPLICATION_JSON),
                   "application/json;charset=UTF-8",
                   responseHandler);
    }

    private void post(String url,
                     String params,
                     AsyncHttpResponseHandler responseHandler)
    {
        Log.d("YadomsRestClient", "POST : " + url + ", params : " + (params != null ? params : ""));
        client.post(context,
                    getAbsoluteUrl(url),
                    new StringEntity(params != null ? params : "", ContentType.APPLICATION_JSON),
                    "application/json;charset=UTF-8",
                    responseHandler);
    }

    void readKeyword(int keywordId)
    {

    }

    void command(int keywordId,
                 boolean command,
                 YadomsRestResponseHandler responseHandler)
    {
        command(keywordId,
                command ? "1" : "0",
                responseHandler);
    }

    private void command(int keywordId,
                         String command,
                         final YadomsRestResponseHandler responseHandler)
    {
        post("/rest/device/keyword/" + keywordId + "/command",
                command,
                new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response)
                    {
                        Log.d("yadomsRestClient", "onSuccess, statusCode = " + statusCode);
                        responseHandler.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable error,
                                          JSONObject errorResponse)
                    {
                        Log.e("yadomsRestClient",
                                "onFailure, statusCode = " + statusCode + ", " + error);

                        switch (statusCode)
                        {
                            case HttpURLConnection.HTTP_UNAUTHORIZED:
                            case HttpURLConnection.HTTP_FORBIDDEN:
                                Toast.makeText(context,
                                        context.getString(R.string.unauthorized),
                                        Toast.LENGTH_LONG).show();
                                break;
                            case HttpURLConnection.HTTP_NOT_FOUND:
                            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                                Toast.makeText(context,
                                        context.getString(R.string.url_not_found),
                                        Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(context,
                                        context.getString(R.string.unknown_error),
                                        Toast.LENGTH_LONG).show();
                                break;
                        }

                        responseHandler.onFailure();
                    }
                });

    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return baseUrl + relativeUrl;
    }
}

class YadomsRestResponseHandler
{
    void onSuccess()
    {
    }

    void onFailure()
    {
    }
}

