package com.desertkun.brainout.common.msg.server;

import org.json.JSONObject;

public class SocialDeletedMsg
{
    public String messageId;
    public String sender;

    public SocialDeletedMsg() {}
    public SocialDeletedMsg(String messageId, String sender)
    {
        this.messageId = messageId;
        this.sender = sender;
    }
}
