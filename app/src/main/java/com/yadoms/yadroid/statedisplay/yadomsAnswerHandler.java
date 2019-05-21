package com.yadoms.yadroid.statedisplay;

interface yadomsAnswerHandler {
    void onDone();

    void onError(int responseCode, String responseMessage);
}
