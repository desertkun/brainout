package com.desertkun.brainout.common.msg.server;

public class AnalyticsEventMsg
{
    public enum Kind
    {
        design,
        progression
    }

    public Kind kind;
    public String[] keys;
    public float value;

    public AnalyticsEventMsg() {}
    public AnalyticsEventMsg(Kind kind, float value, String... keys )
    {
        this.kind = kind;
        this.keys = keys;
        this.value = value;
    }
}
