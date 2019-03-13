package com.yadoms.widgets.statedisplay;

interface yadomsAnswerHandler {
    void onDone();

    void onError(int responseCode, String responseMessage);
}
