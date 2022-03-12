package com.desertkun.brainout.common.msg.server;

import java.util.Date;

public class SocialBatchMsg
{
    public SocialMsg[] messages;
    public LastReadMsg[] lastReadMessages;

    public static class LastReadMsg
    {
        public String recipientClass;
        public String recipientKey;
        public String messageId;
        public Date time;

        public LastReadMsg() {}
        public LastReadMsg(String recipientClass, String recipientKey, String messageId, Date time)
        {
            this.recipientClass = recipientClass;
            this.recipientKey = recipientKey;
            this.messageId = messageId;
            this.time = time;
        }
    }


    public SocialBatchMsg() {}
    public SocialBatchMsg(SocialMsg[] messages, LastReadMsg[] lastReadMessages)
    {
        this.messages = messages;
        this.lastReadMessages = lastReadMessages;
    }
}
