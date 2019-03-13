package com.yadoms.widgets.statedisplay;

import android.util.Base64;
import android.util.Log;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class yadomsRequest
        implements Runnable
{
    private final widgetPref prefs;
    private final boolean state;

    yadomsRequest(widgetPref prefs,
                  boolean state)
    {
        this.prefs = prefs;
        this.state = state;
    }

    @Override
    public void run()
    {
        try
        {
            //TODO les trims devraient être faits à la saisie
            URL url = new URL("http://" + prefs.yadomsServerAddress.trim() + ":" + prefs.yadomsServerPort.trim() + "/rest/device/keyword/" + prefs.keyword.trim() + "/command");
            Log.d("run", "request = " + url);
            Log.d("run", "data = " + (state ? "1" : "0"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            // Basic authentication
            if (prefs.yadomsBasicAuthenticationEnable)
            {
                String login = prefs.basicAuthenticationUsername + ":" + prefs.basicAuthenticationPassword;
                String encodedAuthorization = Base64.encodeToString(login.getBytes(), Base64.DEFAULT);
                connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
            }

            connection.setDoOutput(true);
            connection.setDoInput(true);

            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(state ? "1" : "0");

            os.flush();
            os.close();


            Log.i("STATUS", String.valueOf(connection.getResponseCode()));
            Log.i("MSG", connection.getResponseMessage());

            connection.disconnect();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
