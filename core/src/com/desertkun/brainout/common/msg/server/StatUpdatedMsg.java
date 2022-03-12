package com.desertkun.brainout.common.msg.server;

public class StatUpdatedMsg
{
    public String statId;
    public float value;

    public StatUpdatedMsg() {}
    public StatUpdatedMsg(String statId, float value)
    {
        this.statId = statId;
        this.value = value;
    }
}
