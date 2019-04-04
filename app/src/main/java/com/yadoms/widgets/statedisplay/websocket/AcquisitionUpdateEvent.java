package com.yadoms.widgets.statedisplay.websocket;

class AcquisitionUpdateEvent {
    private int keywordId;
    private String value;

    AcquisitionUpdateEvent(int keywordId, String value) {

        this.keywordId = keywordId;
        this.value = value;
    }

    int getKeywordId() {
        return keywordId;
    }
    String getValue() {
        return value;
    }
}
