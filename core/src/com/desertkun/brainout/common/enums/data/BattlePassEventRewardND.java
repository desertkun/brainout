package com.desertkun.brainout.common.enums.data;

public class BattlePassEventRewardND extends NotifyData
{
    public int eventId;
    public int idx;

    public BattlePassEventRewardND() {}
    public BattlePassEventRewardND(int eventId, int idx)
    {
        this.eventId = eventId;
        this.idx = idx;
    }
}
