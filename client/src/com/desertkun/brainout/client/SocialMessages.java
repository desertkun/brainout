package com.desertkun.brainout.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.MarkMessageReadMsg;
import com.desertkun.brainout.events.SocialMessageDeletedEvent;
import com.desertkun.brainout.events.SocialMessageEvent;
import com.desertkun.brainout.events.SocialMessageUpdatedEvent;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MessageService;
import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;

public class SocialMessages
{
    private OrderedMap<String, ClientMessage> messages;
    private ObjectMap<String, LastReadMessage> lastReadMessages;

    private class LastReadMessage
    {
        public Date time;
        public String messageId;
    }

    public class ClientMessage
    {
        public final String messageType;
        public final String recipientClass;
        public final String recipientKey;
        public final String messageId;
        public final String sender;
        public JSONObject payload;
        public final Date time;

        public ClientMessage(String messageType, String recipientClass, String recipientKey,
                             String messageId, String sender, JSONObject payload, Date time)
        {
            this.messageType = messageType;
            this.recipientClass = recipientClass;
            this.recipientKey = recipientKey;
            this.messageId = messageId;
            this.sender = sender;
            this.payload = payload;
            this.time = time;
        }

        public void markAsRead()
        {
            String key = recipientClass + "." + recipientKey;
            LastReadMessage lastReadMessage = lastReadMessages.get(key);

            if (lastReadMessage != null)
            {
                if (lastReadMessage.messageId.equals(messageId))
                {
                    return;
                }
            }
            else
            {
                lastReadMessage = new LastReadMessage();
                lastReadMessages.put(key, lastReadMessage);
            }

            lastReadMessage.messageId = messageId;
            lastReadMessage.time = time;

            BrainOutClient.ClientController.sendTCP(new MarkMessageReadMsg(this.messageId));
        }

        public boolean isRead()
        {
            if (sender.equals(BrainOutClient.ClientController.getMyAccount()))
            {
                return true;
            }

            LastReadMessage lastRead = lastReadMessages.get(this.recipientClass + "." + this.recipientKey);

            return lastRead != null &&
                (Objects.equals(lastRead.messageId, this.messageId) || lastRead.time.after(this.time));

        }
    }

    public boolean haveUnreadMessages(String recipientClass, String recipientKey)
    {
        LastReadMessage lastRead = lastReadMessages.get(recipientClass + "." + recipientKey);

        for (ObjectMap.Entry<String, ClientMessage> entry : messages)
        {
            ClientMessage message = entry.value;

            if (!message.recipientClass.equals(recipientClass))
                continue;

            if (!message.recipientKey.equals(recipientKey))
                continue;

            if (lastRead == null)
                return true;

            if (Objects.equals(lastRead.messageId, message.messageId))
                return false;

            if (lastRead.time.after(message.time))
                return false;

            if (!message.isRead())
                return true;
        }

        return false;
    }

    public boolean haveUnreadMessages()
    {
        for (ObjectMap.Entry<String, ClientMessage> entry : messages)
        {
            ClientMessage message = entry.value;

            if (!message.isRead())
                return true;
        }

        return false;
    }

    public SocialMessages()
    {
        messages = new OrderedMap<>();
        lastReadMessages = new ObjectMap<>();
    }

    public ClientMessage addMessage(String messageType, String recipientClass, String recipientKey,
        String messageId, String sender, JSONObject payload, Date time,
        boolean send, boolean notify)
    {
        ClientMessage message = new ClientMessage(
            messageType, recipientClass, recipientKey, messageId, sender, payload, time);

        Gdx.app.postRunnable(() ->
        {
            messages.put(messageId, message);
            BrainOut.EventMgr.sendEvent(SocialMessageEvent.obtain(message, notify));
        });

        return message;
    }

    public OrderedMap<String, ClientMessage> getMessages()
    {
        return messages;
    }

    public void clearMessages()
    {
        messages.clear();
        lastReadMessages.clear();
    }

    public void deleteMessage(String messageId, String sender)
    {
        ClientMessage message = messages.remove(messageId);

        if (message != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(SocialMessageDeletedEvent.obtain(messageId));
        }
    }

    public void updateMessage(String messageId, String sender, JSONObject payload)
    {
        ClientMessage message = messages.get(messageId);

        if (message != null)
        {
            message.payload = payload;

            BrainOut.EventMgr.sendDelayedEvent(SocialMessageUpdatedEvent.obtain(messageId, payload));
        }
    }

    public void setLastReadMessage(String recipientClass, String recipientKey, Date time, String messageId)
    {
        LastReadMessage msg = lastReadMessages.get(recipientClass + "." + recipientKey);

        if (msg == null)
        {
            msg = new LastReadMessage();
            lastReadMessages.put(recipientClass + "." + recipientKey, msg);
        }

        msg.messageId = messageId;
        msg.time = time;
    }
}
