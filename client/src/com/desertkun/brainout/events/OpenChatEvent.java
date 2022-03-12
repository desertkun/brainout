package com.desertkun.brainout.events;

public class OpenChatEvent extends Event
{
    public String key;

    @Override
    public ID getID()
    {
        return ID.openChat;
    }

    private Event init(String key)
    {
        this.key = key;

        return this;
    }

    public static Event obtain(String key)
    {
        OpenChatEvent e = obtain(OpenChatEvent.class);
        if (e == null) return null;
        return e.init(key);
    }

    @Override
    public void reset()
    {
        this.key = null;
    }
}
