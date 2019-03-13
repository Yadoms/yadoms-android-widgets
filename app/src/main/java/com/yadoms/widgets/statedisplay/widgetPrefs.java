package com.yadoms.widgets.statedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class widgetPref
{
    private static final String PREF_PREFIX_KEY = "appwidget_";

    private final Context context;
    private final int appWidgetId;

    String yadomsServerAddress;
    String yadomsServerPort;
    boolean yadomsBasicAuthenticationEnable;
    String basicAuthenticationUsername;
    String basicAuthenticationPassword;
    String keyword;

    widgetPref(Context context,
               int appWidgetId)
    {
        this.context = context;
        this.appWidgetId = appWidgetId;
        load();
    }

    private void load()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        yadomsServerAddress = prefs.getString("server_url", null);
        yadomsServerPort = prefs.getString("server_port", "8080");
        yadomsBasicAuthenticationEnable = prefs.getBoolean("basic_authentication", false);
        basicAuthenticationUsername = prefs.getString("basic_authentication_username", null);
        basicAuthenticationPassword = prefs.getString("basic_authentication_password", null);
        keyword = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "keyword", null);
    }

    void save()
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "keyword", keyword);
        prefs.apply();
    }

    void delete()
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "keyword");
        prefs.apply();
    }
}