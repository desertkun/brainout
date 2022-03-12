package com.desertkun.brainout.events;

public class SettingsUpdatedEvent extends Event
{
    @Override
    public ID getID()
    {
        return ID.settingsUpdated;
    }

    private Event init()
    {
        return this;
    }

    public static Event obtain()
    {
        SettingsUpdatedEvent e = obtain(SettingsUpdatedEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
    }
}
