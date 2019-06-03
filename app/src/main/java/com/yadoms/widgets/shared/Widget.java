package com.yadoms.widgets.shared;

public class Widget {
    public final int id;
    public final String className;
    public final int keywordId;
    public final String label;

    public Widget(int id,
                  String className,
                  int keywordId,
                  String label) {
        this.id = id;
        this.className = className;
        this.keywordId = keywordId;
        this.label = label;
    }
}
