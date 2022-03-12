package com.desertkun.brainout.common.msg.client.cards;

import com.desertkun.brainout.common.msg.ModeMessage;

public class CardMessage implements ModeMessage
{
    public String d;
    public int o;

    public CardMessage() {}

    public CardMessage(String d, int o)
    {
        this.d = d;
        this.o = o;
    }
}
