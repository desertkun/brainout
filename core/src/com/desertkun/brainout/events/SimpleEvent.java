package com.desertkun.brainout.events;

public class SimpleEvent extends Event
{
    private Action action;

    public enum Action
    {
        reload,
        refresh,
        disconnect,
        teamSelected,
        teamUpdated,
        pingUpdated,
        playerInfoUpdated,
        consumablesUpdated,
        consumablesUpdatedSingly,
        instrumentUpdated,
        movingCompleted,
        flagTakeChanged,
        invalidSpawn,
        updateSpawn,
        deselected,
        userProfileUpdated,
        immortalEnded,
        audioUpdated,
        eventsUpdated,
        rightsUpdated,
        socialMessagesReceived,
        updateSocialMessages,
        clanInfoUdated,
        activate,
        modeUpdated,
        skinUpdated,
        upgradesUpdated,
        playersStatsUpdated
    }

    public SimpleEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.simple;
    }

    public Action getAction() {
        return action;
    }

    private Event init(Action action)
    {
        this.action = action;

        return this;
    }

    public static Event obtain(Action action)
    {
        SimpleEvent e = obtain(SimpleEvent.class);
        if (e == null) return null;
        return e.init(action);
    }

    @Override
    public void reset()
    {
    }
}
