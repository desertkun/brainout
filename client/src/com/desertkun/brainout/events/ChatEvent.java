package com.desertkun.brainout.events;

import com.desertkun.brainout.common.msg.server.ChatMsg;

public class ChatEvent extends Event
{
    public ChatMsg message;

    public boolean isTerminal()
    {
        return message.isTerminal();
    }

    @Override
    public ID getID()
    {
        return ID.chat;
    }

    private Event init(ChatMsg chatMsg)
    {
        this.message = chatMsg;

        return this;
    }

    public static Event obtain(ChatMsg chatMsg)
    {
        ChatEvent e = obtain(ChatEvent.class);
        if (e == null) return null;
        return e.init(chatMsg);
    }

    @Override
    public void reset()
    {
        this.message = null;
    }
}
