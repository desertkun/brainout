package com.desertkun.brainout.common.msg.server;

import org.json.JSONObject;

public class RequestSuccessMsg
{
    public int id;
    public String args;

    public RequestSuccessMsg() {}
    public RequestSuccessMsg(JSONObject args, int id)
    {
        this.args = args.toString();
        this.id = id;
    }
}
