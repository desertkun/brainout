package com.desertkun.brainout.common.msg.client;

import java.util.Date;

public class MarkMessageReadMsg
{
    public String messageId;

    public MarkMessageReadMsg() {}
    public MarkMessageReadMsg(String messageId)
    {
        this.messageId = messageId;
    }
}
