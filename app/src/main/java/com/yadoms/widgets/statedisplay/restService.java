package com.yadoms.widgets.statedisplay;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class restService extends IntentService {
    private static final String ACTION_YADOMS_REQUEST = "com.yadoms.widgets.statedisplay.action.yadomsRequest";

    private static final String YADOMS_REQUEST_KEYWORD = "com.yadoms.widgets.statedisplay.extra.keyword";
    private static final String YADOMS_REQUEST_NEW_STATE = "com.yadoms.widgets.statedisplay.extra.newState";

    public restService() {
        super("restService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void sendYadomsRequest(Context context,
                                         String keyword,
                                         boolean newState) {
        Intent intent = new Intent(context, restService.class);
        intent.setAction(ACTION_YADOMS_REQUEST);
        intent.putExtra(YADOMS_REQUEST_KEYWORD, keyword);
        intent.putExtra(YADOMS_REQUEST_NEW_STATE, newState);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_YADOMS_REQUEST.equals(action)) {
                handleActionSendYadomsRequest(
                        intent.getStringExtra(YADOMS_REQUEST_KEYWORD),
                        intent.getBooleanExtra(YADOMS_REQUEST_NEW_STATE, false),
                        new yadomsAnswerHandler() {
                            @Override
                            public void onDone() {
                                updateSwitchWidget();
                            }

                            @Override
                            public void onError(int responseCode, String responseMessage) {
                                switch (responseCode) {
                                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                                    case HttpURLConnection.HTTP_FORBIDDEN:
                                        showToast(getApplicationContext().getString(R.string.unauthorized), Toast.LENGTH_LONG);
                                        break;
                                    case HttpURLConnection.HTTP_NOT_FOUND:
                                    case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                                        showToast(getApplicationContext().getString(R.string.url_not_found), Toast.LENGTH_LONG);
                                        break;
                                    case 0:
                                        showToast(getApplicationContext().getString(R.string.unknown_error), Toast.LENGTH_LONG);
                                        break;
                                    default:
                                        showToast(responseMessage, Toast.LENGTH_LONG);
                                        break;
                                }
                            }
                        });
            }
        }
    }

    private void updateSwitchWidget() {
        Intent intent = new Intent(this, SwitchAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), SwitchAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private void handleActionSendYadomsRequest(String keyword,
                                               boolean newState,
                                               yadomsAnswerHandler answerHandler) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String yadomsServerAddress = prefs.getString("server_url", null);
            String yadomsServerPort = prefs.getString("server_port", "8080");
            boolean yadomsBasicAuthenticationEnable = prefs.getBoolean("basic_authentication", false);
            String basicAuthenticationUsername = prefs.getString("basic_authentication_username", null);
            String basicAuthenticationPassword = prefs.getString("basic_authentication_password", null);

            //TODO les trims devraient être faits à la saisie
            URL url = new URL("http://" + yadomsServerAddress.trim() + ":" + yadomsServerPort.trim() + "/rest/device/keyword/" + keyword.trim() + "/command");
            Log.d("run", "request = " + url);
            Log.d("run", "data = " + (newState ? "1" : "0"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setReadTimeout(5000);

            // Basic authentication
            if (yadomsBasicAuthenticationEnable) {
                String login = basicAuthenticationUsername + ":" + basicAuthenticationPassword;
                String encodedAuthorization = Base64.encodeToString(login.getBytes(), Base64.DEFAULT);
                connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
            }

            connection.setDoOutput(true);
            connection.setDoInput(true);

            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(newState ? "1" : "0");

            os.flush();
            os.close();


            Log.i("STATUS", String.valueOf(connection.getResponseCode()));
            Log.i("MSG", connection.getResponseMessage());

            connection.disconnect();

            if (connection.getResponseCode() == 200)
                answerHandler.onDone();
            else
                answerHandler.onError(connection.getResponseCode(), connection.getResponseMessage());
        } catch (java.net.SocketTimeoutException e) {
            answerHandler.onError(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, "");
        } catch (Exception e) {
            e.printStackTrace();
            answerHandler.onError(0, "");
        }
    }

    private void showToast(final String message, final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, duration).show();
            }
        });
    }
}
