package com.desertkun.brainout.common.enums.data;

public class ContentPayloadND extends NotifyData
{
    public String content;
    public String payload;

    public ContentPayloadND() {}
    public ContentPayloadND(String content, String payload)
    {
        this.content = content;
        this.payload = payload;
    }
}
