package com.yadoms.yadroid.statedisplay;

import org.json.JSONException;
import org.json.JSONObject;

class Device {
    private final int id;
    private final String friendlyName;

    public Device(JSONObject json) throws JSONException {
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
