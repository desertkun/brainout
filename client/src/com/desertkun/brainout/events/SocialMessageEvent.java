package com.desertkun.brainout.events;

import com.desertkun.brainout.client.SocialMessages;

public class SocialMessageEvent extends Event
{
    public SocialMessages.ClientMessage message;
    public boolean notify;

    @Override
    public ID getID()
    {
        return ID.socialMessage;
    }

    private Event init(SocialMessages.ClientMessage message, boolean notify)
    {
        this.message = message;
        this.notify = notify;

        return this;
    }

    public static Event obtain(SocialMessages.ClientMessage message, boolean notify)
    {
        SocialMessageEvent e = obtain(SocialMessageEvent.class);
        if (e == null) return null;
        return e.init(message, notify);
    }

    @Override
    public void reset()
    {
        this.message = null;
        this.notify = false;
    }
}
