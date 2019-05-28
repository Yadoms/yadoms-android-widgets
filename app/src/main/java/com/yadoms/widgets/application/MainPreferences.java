package com.yadoms.widgets.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainPreferences
{
    private static MainPreferences MainPreferences;

    public final String yadomsServerAddress;
    public final String yadomsServerPort;
    public final boolean basicAuthenticationEnable;
    public final String basicAuthenticationUsername;
    public final String basicAuthenticationPassword;

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

    public static MainPreferences get(Context context) throws InvalidConfigurationException
    {
        if (MainPreferences == null)
        {
             MainPreferences = new MainPreferences(context);
        }
        return MainPreferences;
    }

    static void invalid() {
        MainPreferences = null;
    }

    public String getServerBaseUrl()
    {
        //TODO les trim devraient être à la saisie
        return "http://" + yadomsServerAddress.trim() + ":" + yadomsServerPort.trim();
    }
}
