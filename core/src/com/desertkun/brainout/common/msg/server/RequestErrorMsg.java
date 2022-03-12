package com.desertkun.brainout.common.msg.server;

import org.json.JSONObject;

public class RequestErrorMsg
{
    public int id;
    public String reason;

    public RequestErrorMsg() {}
    public RequestErrorMsg(String reason, int id)
    {
        this.reason = reason;
        this.id = id;
    }
}
