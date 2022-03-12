package com.desertkun.brainout.common.msg.client;

public class HelloMsg
{
    public String version;
    public String key;
    public String accessToken;
    public int reconnect;

    public HelloMsg() {}

    public HelloMsg(String version, String key, String accessToken)
    {
        this(version, key, accessToken, -1);
    }

    public HelloMsg(String version, String key, String accessToken, int reconnect)
    {
        this.version = version;
        this.key = key;
        this.accessToken = accessToken;
        this.reconnect = reconnect;
    }
}
