package com.desertkun.brainout.common.msg.server;

import org.json.JSONObject;

public class OnlineEventsInfoMsg
{
    public String data;

    public OnlineEventsInfoMsg() {}
    public OnlineEventsInfoMsg(JSONObject data)
    {
        this.data = data.toString();
    }
}
