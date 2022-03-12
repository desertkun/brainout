package com.desertkun.brainout.common.msg.server;

import org.json.JSONObject;

public class SocialUpdatedMsg
{
    public String messageId;
    public String sender;
    public String payload;

    public SocialUpdatedMsg() {}
    public SocialUpdatedMsg(String messageId, String sender, JSONObject payload)
    {
        this.messageId = messageId;
        this.sender = sender;
        this.payload = payload.toString();
    }
}
