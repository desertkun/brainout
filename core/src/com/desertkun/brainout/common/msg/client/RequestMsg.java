package com.desertkun.brainout.common.msg.client;

import com.badlogic.gdx.utils.ObjectMap;
import org.json.JSONObject;

public class RequestMsg
{
    public String method;
    public String args;
    public int id;

    public RequestMsg() {}
    public RequestMsg(String method, JSONObject args, int id)
    {
        this.method = method;
        this.args = args.toString();
        this.id = id;
    }
}
