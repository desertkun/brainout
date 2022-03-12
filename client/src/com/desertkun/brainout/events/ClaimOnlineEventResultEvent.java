package com.desertkun.brainout.events;

public class ClaimOnlineEventResultEvent extends Event
{
    public int eventId;
    public int rewardIndex;
    public boolean success;

    @Override
    public ID getID()
    {
        return ID.onlineEventClaimResult;
    }

    private Event init(int eventId, int rewardIndex, boolean success)
    {
        this.eventId = eventId;
        this.rewardIndex = rewardIndex;
        this.success = success;

        return this;
    }

    public static Event obtain(int eventId, int rewardIndex, boolean success)
    {
        ClaimOnlineEventResultEvent e = obtain(ClaimOnlineEventResultEvent.class);
        if (e == null) return null;
        return e.init(eventId, rewardIndex, success);
    }

    @Override
    public void reset()
    {
        this.eventId = 0;
        this.rewardIndex = 0;
        this.success = false;
    }
}
