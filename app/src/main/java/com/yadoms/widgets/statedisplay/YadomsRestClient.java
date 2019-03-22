package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

public class YadomsRestClient
{
    private final String baseUrl;
    private final Context applicationContext;

    YadomsRestClient(Context applicationContext)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);

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
        this.applicationContext = applicationContext;
    }

    private AsyncHttpClient client = new AsyncHttpClient();

    public void get(String url,
                    String params,
                    AsyncHttpResponseHandler responseHandler)
    {
        Log.d("YadomsRestClient", "GET : " + url + ", params : " + (params != null ? params : ""));
        client.get(applicationContext,
                   getAbsoluteUrl(url),
                   new StringEntity(params != null ? params : "", ContentType.APPLICATION_JSON),
                   "application/json;charset=UTF-8",
                   responseHandler);
    }

    public void post(String url,
                     String params,
                     AsyncHttpResponseHandler responseHandler)
    {
        Log.d("YadomsRestClient", "POST : " + url + ", params : " + (params != null ? params : ""));
        client.post(applicationContext,
                    getAbsoluteUrl(url),
                    new StringEntity(params != null ? params : "", ContentType.APPLICATION_JSON),
                    "application/json;charset=UTF-8",
                    responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return baseUrl + relativeUrl;
    }
}

