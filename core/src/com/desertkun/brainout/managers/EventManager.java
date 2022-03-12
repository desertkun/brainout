package com.desertkun.brainout.managers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;

public class EventManager
{
    public class SendPair
    {
        public EventReceiver receiver;
        public Event event;
    }

    private class SubscribePair
    {
        public Event.ID eventId;
        public EventReceiver receiver;
        public boolean first;
    }

    private final Queue<SendPair> futurePairs, pairs;
    private ObjectMap<Event.ID, Queue<EventReceiver>> subscribers;
    private Queue<SubscribePair> subscribersList, unsibscribersList;
    private boolean calling;
    private final Thread mainThread;

    public EventManager()
    {
        futurePairs = new Queue<>();
        pairs = new Queue<>();
        subscribers = new ObjectMap<>();
        subscribersList = new Queue<>();
        unsibscribersList = new Queue<>();
        calling = false;
        mainThread = Thread.currentThread();
    }

    private void subscribe(SubscribePair pair)
    {
        Queue<EventReceiver> eventReceivers = subscribers.get(pair.eventId);
        if (eventReceivers == null)
        {
            eventReceivers = new Queue<>();
            subscribers.put(pair.eventId, eventReceivers);
        }

        if (eventReceivers.indexOf(pair.receiver, true) >= 0)
        {
            // already subscribed
            return;
        }

        if (pair.first)
        {
            eventReceivers.addFirst(pair.receiver);
        }
        else
        {
            eventReceivers.addLast(pair.receiver);
        }
    }

    public void subscribe(Event.ID eventID, EventReceiver receiver)
    {
        subscribeAt(eventID, receiver, false);
    }

    public void subscribeAt(Event.ID eventID, EventReceiver receiver, boolean fist)
    {
        SubscribePair subscribePair = new SubscribePair();

        subscribePair.eventId = eventID;
        subscribePair.receiver = receiver;
        subscribePair.first = fist;

        subscribersList.addLast(subscribePair);
    }

    private void unsubscribe(SubscribePair pair)
    {
        Queue<EventReceiver> eventReceivers = subscribers.get(pair.eventId);

        if (eventReceivers != null)
        {
            if (pair.receiver == null)
            {
                eventReceivers.clear();

                return;
            }

            eventReceivers.removeValue(pair.receiver, true);
        }
    }

    public void unsubscribe(Event.ID eventID, EventReceiver receiver)
    {
        SubscribePair subscribePair = new SubscribePair();

        subscribePair.eventId = eventID;
        subscribePair.receiver = receiver;

        unsibscribersList.addLast(subscribePair);
    }

    public void unsubscribeAll(Event.ID eventID)
    {
        SubscribePair subscribePair = new SubscribePair();

        subscribePair.eventId = eventID;
        subscribePair.receiver = null;

        unsibscribersList.addLast(subscribePair);
    }

    public boolean sendEvent(Event event)
    {
        return sendEvent(event, true);
    }

    public boolean sendEvent(Event event, boolean free)
    {
        if (event == null)
            return false;

        Queue<EventReceiver> eventReceivers = subscribers.get(event.getID());

        try
        {
            if (checkCalling())
            {
                if (eventReceivers == null)
                {
                    return false;
                }

                for (EventReceiver receiver: eventReceivers)
                {
                    if (sendEvent(receiver, event, false))
                    {
                        return true;
                    }
                }
            }
            /*
            else
            {
                throw new RuntimeException("You fucker.");
            }
            */
        }
        finally
        {
            if (free)
                event.free();
        }

        return false;
    }

    private boolean checkCalling()
    {
        if (mainThread != Thread.currentThread())
        {
            throw new RuntimeException("Trying to send event in onEvent on a different thread than main.");
        }

        return !calling;
    }

    public boolean sendEvent(EventReceiver receiver, Event event)
    {
        return sendEvent(receiver, event, true);
    }

    public boolean sendEvent(EventReceiver receiver, Event event, boolean free)
    {
        if (event == null)
            return false;

        try
        {
            if (checkCalling())
            {
                calling = true;

                if (receiver == null)
                {
                    return false;
                }

                boolean res = receiver.onEvent(event);

                calling = false;

                return res;
            }
        }
        finally
        {
            if (free)
                event.free();
        }

        return false;
    }

    public void sendDelayedEvent(EventReceiver receiver, Event event)
    {
        if (event == null)
            return;

        synchronized (futurePairs)
        {
            SendPair sendPair = new SendPair();
            sendPair.receiver = receiver;
            sendPair.event = event;

            futurePairs.addLast(sendPair);
        }
    }

    public void sendDelayedEvent(Event event)
    {
        if (event == null)
            return;

        synchronized (futurePairs)
        {
            SendPair sendPair = new SendPair();
            sendPair.receiver = null;
            sendPair.event = event;

            futurePairs.addLast(sendPair);
        }
    }

    public void update()
    {
        for (SubscribePair subscribePair: subscribersList)
        {
            subscribe(subscribePair);
        }

        for (SubscribePair subscribePair: unsibscribersList)
        {
            unsubscribe(subscribePair);
        }

        if (subscribersList.size > 0)
            subscribersList.clear();

        if (unsibscribersList.size > 0)
            unsibscribersList.clear();

        while (true)
        {
            for (SendPair sendPair: pairs)
            {
                if (sendPair == null) continue;

                try
                {
                    if (sendPair.receiver != null)
                    {
                        sendEvent(sendPair.receiver, sendPair.event, false);
                    }
                    else
                    {
                        sendEvent(sendPair.event, false);
                    }
                }
                finally
                {
                    sendPair.event.free();
                }
            }

            pairs.clear();

            synchronized (futurePairs)
            {
                if (futurePairs.size > 0)
                {
                    for (SendPair pair : futurePairs)
                    {
                        pairs.addLast(pair);
                    }

                    futurePairs.clear();
                } else
                {
                    break;
                }
            }
        }
    }

    public ObjectMap<Event.ID, Queue<EventReceiver>> getSubscribers()
    {
        return subscribers;
    }
}
