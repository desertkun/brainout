package com.desertkun.brainout.common.msg.server;

import org.json.JSONObject;

import java.util.Date;

public class SocialMsg
{
    public String messageType;
    public String recipientClass;
    public String recipientKey;
    public String messageId;
    public String sender;
    public String payload;
    public Date time;

    public SocialMsg() {}
    public SocialMsg(String messageType, String recipientClass, String recipientKey,
                     String messageId, String sender, JSONObject payload, Date time)
    {
        this.messageType = messageType;
        this.recipientClass = recipientClass;
        this.recipientKey = recipientKey;
        this.messageId = messageId;
        this.sender = sender;
        this.payload = payload.toString();
        this.time = time;
    }
}
