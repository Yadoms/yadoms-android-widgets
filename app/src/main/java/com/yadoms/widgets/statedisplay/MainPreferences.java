package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class MainPreferences
{
    private static MainPreferences MainPreferences;

    final String yadomsServerAddress;
    final String yadomsServerPort;
    final boolean basicAuthenticationEnable;
    final String basicAuthenticationUsername;
    final String basicAuthenticationPassword;

    private MainPreferences(Context context) throws InvalidConfigurationException
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        yadomsServerAddress = prefs.getString("server_url", null);
        yadomsServerPort = prefs.getString("server_port", "8080");

        if (yadomsServerAddress == null || yadomsServerAddress.isEmpty()
                || yadomsServerPort == null || yadomsServerPort.isEmpty())
            throw new InvalidConfigurationException("server_url or server_port not defined");

        basicAuthenticationEnable = prefs.getBoolean("basic_authentication", false);
        basicAuthenticationUsername = prefs.getString("basic_authentication_username", null);
        basicAuthenticationPassword = prefs.getString("basic_authentication_password", null);
    }

    static MainPreferences get(Context context) throws InvalidConfigurationException
    {
        if (MainPreferences == null)
        {
             MainPreferences = new MainPreferences(context);
        }
        return MainPreferences;
    }

    String getServerBaseUrl()
    {
        //TODO les trim devraient être à la saisie
        return "http://" + yadomsServerAddress.trim() + ":" + yadomsServerPort.trim();
    }
}
