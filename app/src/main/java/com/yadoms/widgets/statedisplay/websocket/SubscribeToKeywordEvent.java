package com.yadoms.widgets.statedisplay.websocket;

class SubscribeToKeywordEvent {
    private int keywordId;

    SubscribeToKeywordEvent(int keywordId)
    {
        this.keywordId = keywordId;
    }

    int getKeywordId()
    {
        return keywordId;
    }
}
