package com.desertkun.brainout.events;

public class SocialMessageDeletedEvent extends Event
{
    public String messageId;

    @Override
    public ID getID()
    {
        return ID.socialMessageDeleted;
    }

    private Event init(String messageId)
    {
        this.messageId = messageId;

        return this;
    }

    public static Event obtain(String messageId)
    {
        SocialMessageDeletedEvent e = obtain(SocialMessageDeletedEvent.class);
        if (e == null) return null;
        return e.init(messageId);
    }

    @Override
    public void reset()
    {
        this.messageId = null;
    }
}
