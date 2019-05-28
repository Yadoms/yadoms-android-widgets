package com.yadoms.widgets.shared;

public class Widget
{
    public final int id;
    public final int keywordId;
    public final String label;

    public Widget(int id,
                  int keywordId,
                  String label)
    {
        this.id = id;
        this.keywordId = keywordId;
        this.label = label;
    }
}
