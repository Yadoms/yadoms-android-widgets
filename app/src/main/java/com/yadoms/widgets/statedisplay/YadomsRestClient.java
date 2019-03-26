package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
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

    private void processHttpFailure(int statusCode,
                                    Throwable error)
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
    }

    void getAllDevices(final YadomsRestGetResponseHandler responseHandler)
    {
        get("/rest/device",
            "",
            new JsonHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode,
                                      Header[] headers,
                                      JSONObject response)
                {
                    Log.d("yadomsRestClient", "onSuccess, statusCode = " + statusCode);
                    try
                    {
                        if (!response.getString("result").equals("true"))
                        {
                            throw new RuntimeException("Yadoms returned error");
                        }

                        JSONArray devicesArray = response.getJSONObject("data").getJSONArray("device");
                        Device devices[] = new Device[devicesArray.length()];
                        for (int jsonIndex = 0; jsonIndex < devicesArray.length(); ++jsonIndex)
                        {
                            JSONObject json = devicesArray.getJSONObject(jsonIndex);
                            devices[jsonIndex] = new Device(json);
                        }
                        responseHandler.onSuccess(devices);
                    }
                    catch (JSONException e)
                    {
                        Log.w("yadomsRestClient", "Fail to parse /rest/device answer");
                        responseHandler.onFailure();
                    }
                }

                @Override
                public void onFailure(int statusCode,
                                      Header[] headers,
                                      Throwable error,
                                      JSONObject errorResponse)
                {
                    processHttpFailure(statusCode,
                                       error);
                    responseHandler.onFailure();
                }
            });
    }

    void getDevicesWithCapacity(EKeywordAccessMode keywordAccessMode,
                                String capacityName,
                                final YadomsRestGetResponseHandler responseHandler)
    {
        get("/rest/device/matchcapacity/" + keywordAccessMode + "/" + capacityName,
            "",
            new JsonHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode,
                                      Header[] headers,
                                      JSONObject response)
                {
                    Log.d("yadomsRestClient", "onSuccess, statusCode = " + statusCode);
                    try
                    {
                        if (!response.getString("result").equals("true"))
                        {
                            throw new RuntimeException("Yadoms returned error");
                        }

                        JSONArray devicesArray = response.getJSONObject("data").getJSONArray("device");
                        Device devices[] = new Device[devicesArray.length()];
                        for (int jsonIndex = 0; jsonIndex < devicesArray.length(); ++jsonIndex)
                        {
                            JSONObject json = devicesArray.getJSONObject(jsonIndex);
                            devices[jsonIndex] = new Device(json);
                        }
                        responseHandler.onSuccess(devices);
                    }
                    catch (JSONException e)
                    {
                        Log.w("yadomsRestClient", "Fail to parse /rest/device answer");
                        responseHandler.onFailure();
                    }
                }

                @Override
                public void onFailure(int statusCode,
                                      Header[] headers,
                                      Throwable error,
                                      JSONObject errorResponse)
                {
                    processHttpFailure(statusCode,
                                       error);
                    responseHandler.onFailure();
                }
            });
    }

    void getDeviceKeywords(int deviceId,
                           final YadomsRestGetResponseHandler responseHandler)
    {
        get("/rest/device/" + deviceId + "/keyword",
            "",
            new JsonHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode,
                                      Header[] headers,
                                      JSONObject response)
                {
                    Log.d("yadomsRestClient", "onSuccess, statusCode = " + statusCode);
                    try
                    {
                        if (!response.getString("result").equals("true"))
                        {
                            throw new RuntimeException("Yadoms returned error");
                        }

                        JSONArray keywordArray = response.getJSONObject("data").getJSONArray("keyword");
                        Keyword keywords[] = new Keyword[keywordArray.length()];
                        for (int jsonIndex = 0; jsonIndex < keywordArray.length(); ++jsonIndex)
                        {
                            JSONObject json = keywordArray.getJSONObject(jsonIndex);
                            keywords[jsonIndex] = new Keyword(json);
                        }
                        responseHandler.onSuccess(keywords);
                    }
                    catch (JSONException e)
                    {
                        Log.w("yadomsRestClient", "Fail to parse /rest/device answer");
                        responseHandler.onFailure();
                    }
                }

                @Override
                public void onFailure(int statusCode,
                                      Header[] headers,
                                      Throwable error,
                                      JSONObject errorResponse)
                {
                    processHttpFailure(statusCode,
                                       error);
                    responseHandler.onFailure();
                }
            });
    }


    void command(int keywordId,
                 boolean command,
                 YadomsRestCommandResponseHandler responseHandler)
    {
        command(keywordId,
                command ? "1" : "0",
                responseHandler);
    }

    private void command(int keywordId,
                         String command,
                         final YadomsRestCommandResponseHandler responseHandler)
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
                     processHttpFailure(statusCode,
                                        error);
                     responseHandler.onFailure();
                 }
             });

    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return baseUrl + relativeUrl;
    }
}

class YadomsRestCommandResponseHandler
{
    void onSuccess()
    {
    }

    void onFailure()
    {
    }
}

class YadomsRestGetResponseHandler
{
    void onSuccess(Object[] objects)
    {
    }

    void onFailure()
    {
    }
}

