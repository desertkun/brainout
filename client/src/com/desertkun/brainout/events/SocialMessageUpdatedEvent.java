package com.desertkun.brainout.events;

import com.desertkun.brainout.client.SocialMessages;
import org.json.JSONObject;

public class SocialMessageUpdatedEvent extends Event
{
    public String messageId;
    public JSONObject payload;

    @Override
    public ID getID()
    {
        return ID.socialMessageUpdated;
    }

    private Event init(String messageId, JSONObject payload)
    {
        this.messageId = messageId;
        this.payload = payload;

        return this;
    }

    public static Event obtain(String messageId, JSONObject payload)
    {
        SocialMessageUpdatedEvent e = obtain(SocialMessageUpdatedEvent.class);
        if (e == null) return null;
        return e.init(messageId, payload);
    }

    @Override
    public void reset()
    {
        this.messageId = null;
        this.payload = null;
    }
}
