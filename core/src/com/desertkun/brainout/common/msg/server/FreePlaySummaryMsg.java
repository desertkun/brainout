package com.desertkun.brainout.common.msg.server;

import org.json.JSONObject;

public class FreePlaySummaryMsg
{
    public String summary;
    public boolean alive;

    public FreePlaySummaryMsg() {}
    public FreePlaySummaryMsg(JSONObject summary, boolean alive)
    {
        this.summary = summary.toString();
        this.alive = alive;
    }
}
