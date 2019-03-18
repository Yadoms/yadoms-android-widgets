package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class YadomsRestClient
{
    private final String baseUrl;

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
            client.setBasicAuth(basicAuthenticationUsername, basicAuthenticationPassword);
    }

    private AsyncHttpClient client = new AsyncHttpClient();

    public void get(String url,
                    RequestParams params,
                    AsyncHttpResponseHandler responseHandler)
    {
        Log.d("YadomsRestClient", "GET : " + url + ", params : " + params.toString());
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url,
                     RequestParams params,
                     AsyncHttpResponseHandler responseHandler)
    {
        Log.d("YadomsRestClient", "POST : " + url + ", params : " + params.toString());
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return baseUrl + relativeUrl;
    }
}

class SingleStringRequestParam //TODO déplacer ?
        extends RequestParams
{
    private final String value;

    SingleStringRequestParam(String value)
    {
        super();
        this.value = value;
    }

    SingleStringRequestParam(boolean value)
    {
        super();
        this.value = value ? "1" : "0";
    }

    @Override
    public String toString()
    {
        return value;
    }
}