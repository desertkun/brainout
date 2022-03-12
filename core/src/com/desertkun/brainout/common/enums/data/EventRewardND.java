package com.desertkun.brainout.common.enums.data;

public class EventRewardND extends NotifyData
{
    public int eventId;
    public int unlocked;
    public int of;

    public EventRewardND() {}
    public EventRewardND(int eventId, int unlocked, int of)
    {
        this.eventId = eventId;
        this.unlocked = unlocked;
        this.of = of;
    }
}
