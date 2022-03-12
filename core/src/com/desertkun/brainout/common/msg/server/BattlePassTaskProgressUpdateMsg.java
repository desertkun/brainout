package com.desertkun.brainout.common.msg.server;

public class BattlePassTaskProgressUpdateMsg
{
    public int ev;
    public int idx;
    public int unc;
    public int cm;

    public BattlePassTaskProgressUpdateMsg() {}
    public BattlePassTaskProgressUpdateMsg(int eventId, int taskIndex, int uncommitted, int committed)
    {
        this.ev = eventId;
        this.idx = taskIndex;
        this.unc = uncommitted;
        this.cm = committed;
    }
}
