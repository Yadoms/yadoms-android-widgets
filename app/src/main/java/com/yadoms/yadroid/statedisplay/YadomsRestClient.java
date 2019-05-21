package com.yadoms.yadroid.statedisplay;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

class YadomsRestClient {
    private final String baseUrl;
    private final Context context;
    private SyncHttpClient client = new SyncHttpClient();

    YadomsRestClient(Context context) throws InvalidConfigurationException {
        this.context = context;

        MainPreferences mainPreferences = MainPreferences.get(context);
        baseUrl = mainPreferences.getServerBaseUrl();

        if (mainPreferences.basicAuthenticationEnable) {
            client.setBasicAuth(mainPreferences.basicAuthenticationUsername,
                    mainPreferences.basicAuthenticationPassword);
        }
    }

    void withTimeout(int delayMillis) {
        client.setTimeout(delayMillis);
    }

    private void get(final String url,
                     final String params,
                     final ResponseHandlerInterface responseHandler) {
        Log.d(getClass().getSimpleName(), "GET : " + url + ", params : " + (params != null ? params : ""));
        client.get(context,
                getAbsoluteUrl(url),
                new StringEntity(params != null ? params : "", ContentType.APPLICATION_JSON),
                "application/json;charset=UTF-8",
                responseHandler);
    }

    private void post(final String url,
                      final String params,
                      final ResponseHandlerInterface responseHandler) {
        Log.d(getClass().getSimpleName(), "POST : " + url + ", params : " + (params != null ? params : ""));
        client.post(context,
                getAbsoluteUrl(url),
                new StringEntity(params != null ? params : "", ContentType.APPLICATION_JSON),
                "application/json;charset=UTF-8",
                responseHandler);
    }

    private void processHttpFailure(int statusCode,
                                    String requestName,
                                    Throwable error) {

        switch (statusCode) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
            case HttpURLConnection.HTTP_FORBIDDEN:
                Log.e(getClass().getSimpleName(),
                requestName + " : Unauthorized, check authentication settings, statusCode = " + statusCode + ", " + error);
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                Log.e(getClass().getSimpleName(),
                        requestName + " : Can't join server, check connection settings, statusCode = " + statusCode + ", " + error);
                break;
            default:
                Log.e(getClass().getSimpleName(),
                        requestName + " : Unknown error, statusCode = " + statusCode + ", " + error);
                break;
        }
    }

    void getAllDevices(final YadomsRestGetResponseHandler responseHandler) {
        get("/rest/device",
                "",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.d(getClass().getSimpleName(), "onSuccess, statusCode = " + statusCode + ", " + response.toString());
                        try {
                            if (!response.getString("result").equals("true")) {
                                throw new RuntimeException("Yadoms returned error");
                            }

                            JSONArray devicesArray = response.getJSONObject("data").getJSONArray("device");
                            Device devices[] = new Device[devicesArray.length()];
                            for (int jsonIndex = 0; jsonIndex < devicesArray.length(); ++jsonIndex) {
                                JSONObject json = devicesArray.getJSONObject(jsonIndex);
                                devices[jsonIndex] = new Device(json);
                            }
                            responseHandler.onSuccess(devices);
                        } catch (JSONException e) {
                            Log.w(getClass().getSimpleName(), "Fail to parse /rest/device answer");
                            responseHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable error,
                                          JSONObject errorResponse) {
                        processHttpFailure(statusCode,
                                getClass().getSimpleName(),
                                error);
                        responseHandler.onFailure();
                    }
                });
    }

    void getDevicesWithCapacity(EKeywordAccessMode keywordAccessMode,
                                String capacityName,
                                final YadomsRestGetResponseHandler responseHandler) {
        get("/rest/device/matchcapacity/" + keywordAccessMode + "/" + capacityName,
                "",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.d(getClass().getSimpleName(), "onSuccess, statusCode = " + statusCode + ", " + response.toString());
                        try {
                            if (!response.getString("result").equals("true")) {
                                throw new RuntimeException("Yadoms returned error");
                            }

                            JSONArray devicesArray = response.getJSONObject("data").getJSONArray("device");
                            Device devices[] = new Device[devicesArray.length()];
                            for (int jsonIndex = 0; jsonIndex < devicesArray.length(); ++jsonIndex) {
                                JSONObject json = devicesArray.getJSONObject(jsonIndex);
                                devices[jsonIndex] = new Device(json);
                            }
                            responseHandler.onSuccess(devices);
                        } catch (JSONException e) {
                            Log.w(getClass().getSimpleName(), "Fail to parse /rest/device answer");
                            responseHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable error,
                                          JSONObject errorResponse) {
                        processHttpFailure(statusCode,
                                getClass().getSimpleName(),
                                error);
                        responseHandler.onFailure();
                    }
                });
    }

    void getDeviceKeywords(int deviceId,
                           final YadomsRestGetResponseHandler responseHandler) {
        get("/rest/device/" + deviceId + "/keyword",
                "",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.d(getClass().getSimpleName(), "onSuccess, statusCode = " + statusCode + ", " + response.toString());
                        try {
                            if (!response.getString("result").equals("true")) {
                                throw new RuntimeException("Yadoms returned error");
                            }

                            JSONArray keywordArray = response.getJSONObject("data").getJSONArray("keyword");
                            Keyword keywords[] = new Keyword[keywordArray.length()];
                            for (int jsonIndex = 0; jsonIndex < keywordArray.length(); ++jsonIndex) {
                                JSONObject json = keywordArray.getJSONObject(jsonIndex);
                                keywords[jsonIndex] = new Keyword(json);
                            }
                            responseHandler.onSuccess(keywords);
                        } catch (JSONException e) {
                            Log.w(getClass().getSimpleName(), "Fail to parse /rest/device answer");
                            responseHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable error,
                                          JSONObject errorResponse) {
                        processHttpFailure(statusCode,
                                getClass().getSimpleName(),
                                error);
                        responseHandler.onFailure();
                    }
                });
    }

    void getKeywordLastValue(int keywordId,
                             final YadomsRestGetResponseHandler responseHandler) {
        get("/rest/device/keyword/" + keywordId,
                "",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.d(getClass().getSimpleName(), "onSuccess, statusCode = " + statusCode + ", " + response.toString());
                        try {
                            if (!response.getString("result").equals("true")) {
                                throw new RuntimeException("Yadoms returned error");
                            }

                            String lastValue[] = new String[1];
                            lastValue[0] = response.getJSONObject("data").getString("lastAcquisitionValue");
                            responseHandler.onSuccess(lastValue);
                        } catch (JSONException e) {
                            Log.w(getClass().getSimpleName(), "Fail to parse /rest/device/keyword answer");
                            responseHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable error,
                                          JSONObject errorResponse) {
                        processHttpFailure(statusCode,
                                getClass().getSimpleName(),
                                error);
                        responseHandler.onFailure();
                    }
                });
    }

    void command(int keywordId,
                 boolean command,
                 YadomsRestCommandResponseHandler responseHandler) {
        command(keywordId,
                command ? "1" : "0",
                responseHandler);
    }

    private void command(int keywordId,
                         String command,
                         final YadomsRestCommandResponseHandler responseHandler) {
        post("/rest/device/keyword/" + keywordId + "/command",
                command,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.d(getClass().getSimpleName(), "onSuccess, statusCode = " + statusCode + ", " + response.toString());
                        responseHandler.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable error,
                                          JSONObject errorResponse) {
                        processHttpFailure(statusCode,
                                getClass().getSimpleName(),
                                error);
                        responseHandler.onFailure();
                    }
                });

    }

    void getLastEvent(final YadomsRestGetResponseHandler responseHandler) {
        get("/rest/eventLogger/last",
                "",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {
                        Log.d(getClass().getSimpleName(), "onSuccess, statusCode = " + statusCode + ", " + response.toString());
                        try {
                            if (!response.getString("result").equals("true")) {
                                throw new RuntimeException("Yadoms returned error");
                            }

//TODO
                            responseHandler.onSuccess(null);
                        } catch (JSONException e) {
                            Log.w(getClass().getSimpleName(), "Fail to parse rest/eventLogger/last answer");
                            responseHandler.onFailure();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable error,
                                          JSONObject errorResponse) {
                        processHttpFailure(statusCode,
                                getClass().getSimpleName(),
                                error);
                        responseHandler.onFailure();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        processHttpFailure(statusCode,
                                getClass().getSimpleName(),
                                throwable);
                        responseHandler.onFailure();
                    }
                });
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return baseUrl + relativeUrl;
    }
}

class YadomsRestCommandResponseHandler {
    void onSuccess() {
    }

    void onFailure() {
    }
}

class YadomsRestGetResponseHandler {
    void onSuccess(Object[] objects) {
    }

    void onFailure() {
    }
}

