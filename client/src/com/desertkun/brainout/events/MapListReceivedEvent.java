package com.desertkun.brainout.events;

public class MapListReceivedEvent extends Event
{
    public String[] maps;

    @Override
    public ID getID()
    {
        return ID.mapListReceived;
    }

    private Event init(String[] maps)
    {
        this.maps = maps;

        return this;
    }

    public static Event obtain(String[] maps)
    {
        MapListReceivedEvent e = obtain(MapListReceivedEvent.class);
        if (e == null) return null;
        return e.init(maps);
    }

    @Override
    public void reset()
    {
        this.maps = null;
    }
}
