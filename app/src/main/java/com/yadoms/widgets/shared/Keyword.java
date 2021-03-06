package com.yadoms.widgets.shared;

import org.json.JSONException;
import org.json.JSONObject;

public class Keyword {
    private final int id;
    private final String friendlyName;

    public Keyword(JSONObject json) throws JSONException {
        id = json.getInt("id");
        friendlyName = json.getString("friendlyName");
    }

    public int getId() {
        return id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}
