package com.desertkun.brainout.common.msg.client;

public class ClaimOnlineEventRewardMsg
{
    public int eventId;
    public int reward;

    public ClaimOnlineEventRewardMsg() {}
    public ClaimOnlineEventRewardMsg(int eventId, int reward)
    {
        this.eventId = eventId;
        this.reward = reward;
    }
}
