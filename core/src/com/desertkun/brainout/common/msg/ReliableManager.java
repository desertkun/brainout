package com.desertkun.brainout.common.msg;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.Constants;
import com.esotericsoftware.minlog.Log;


import java.util.HashSet;

public abstract class ReliableManager
{
    private int nextId;
    private final ObjectMap<Integer, MessageInfo> messages;
    private final HashSet<Integer> toDelete;

    public class MessageInfo
    {
        public int id;
        public ReliableUdpMessage message;
        public MessageRecipient recipient;
        public long timeOut;
        public int tries;
    }

    public interface MessageRecipient
    {

    }

    public ReliableManager()
    {
        nextId = 0;
        messages = new ObjectMap<Integer, MessageInfo>(2000);
        toDelete = new HashSet<Integer>();
    }

    public int getNextId()
    {
        return nextId++;
    }

    public void deliver(ReliableBody messageBody,
        MessageRecipient messageRecipient)
    {
        int id = getNextId();

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.message = new ReliableUdpMessage(id, messageBody);
        messageInfo.recipient = messageRecipient;
        messageInfo.timeOut = System.currentTimeMillis() + Constants.UdpMessages.TIMEOUT;
        messageInfo.tries = Constants.UdpMessages.RETRIES;

        messages.put(id, messageInfo);

        doDeliver(messageInfo.message, messageRecipient);
    }

    public void delivered(int id)
    {
        synchronized (messages)
        {
            MessageInfo messageInfo = messages.get(id);
            if (messageInfo != null)
            {
                messages.remove(id);
            }
        }
    }

    protected abstract void doDeliver(ReliableUdpMessage message, MessageRecipient messageRecipient);
    protected abstract void onDisconnect(MessageRecipient messageRecipient);

    public void update()
    {
        long tm = System.currentTimeMillis();

        synchronized (messages)
        {
            for (ObjectMap.Entry<Integer, MessageInfo> entry : messages)
            {
                MessageInfo messageInfo = entry.value;

                if (tm > messageInfo.timeOut)
                {
                    if (messageInfo.tries > 0)
                    {
                        if (Log.DEBUG) Log.debug("Message " + entry.key + " timed out!");

                        messageInfo.tries--;
                        messageInfo.timeOut = tm + Constants.UdpMessages.TIMEOUT;
                        doDeliver(messageInfo.message, messageInfo.recipient);
                    }
                    else
                    {
                        if (Log.DEBUG) Log.debug("Message " + entry.key + " timed out and no enough tries!");

                        onDisconnect(messageInfo.recipient);
                        toDelete.add(entry.key);
                    }
                }
            }

            if (!toDelete.isEmpty())
            {
                for (int id: toDelete)
                {
                    messages.remove(id);
                }

                toDelete.clear();
            }
        }

    }
}
