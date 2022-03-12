package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class UnlockSubscription
{
    private ObjectMap<String, Publisher> publishers;
    private ObjectMap<String, Subscribers> subscribers;

    public static class Subscribers extends Array<Subscriber>
    {
        private Array<Subscriber> removeList = new Array<>();

        public void update(float property)
        {
            for (Subscriber subscriber : this)
            {
                subscriber.update(property);

                if (property >= subscriber.getTarget())
                {
                    subscriber.complete();
                    removeList.add(subscriber);
                }
            }

            if (removeList.size > 0)
            {
                for (Subscriber subscriber : removeList)
                {
                    removeValue(subscriber, true);
                }

                removeList.clear();
            }
        }

        @Override
        public void clear()
        {
            super.clear();

            removeList.clear();
        }
    }

    public abstract static class Subscriber
    {
        private int target;

        public Subscriber(int target)
        {
            this.target = target;
        }

        public int getTarget()
        {
            return target;
        }

        public abstract void complete();

        public void update(float value) {}
    }

    public abstract static class Publisher
    {
        private String name;

        public abstract float getProperty();

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    public UnlockSubscription()
    {
        this.publishers = new ObjectMap<>();
        this.subscribers = new ObjectMap<>();
    }

    public Publisher getPublisher(String id)
    {
        return publishers.get(id);
    }

    public void addPublisher(String id, Publisher publisher)
    {
        this.publishers.put(id, publisher);
        publisher.setName(id);
    }

    public ObjectMap<String, Publisher> getPublishers()
    {
        return publishers;
    }

    public Subscribers getSubscribers(String publisher)
    {
        Subscribers subscribers = this.subscribers.get(publisher);

        if (subscribers == null)
        {
            subscribers = new Subscribers();
            this.subscribers.put(publisher, subscribers);
        }

        return subscribers;
    }

    public void addSubscriber(String publisher, Subscriber subscriber)
    {
        getSubscribers(publisher).add(subscriber);
    }

    public void updatePublisher(String publisher)
    {
        Publisher pub = getPublisher(publisher);

        if (pub == null) return;

        getSubscribers(publisher).update(pub.getProperty());
    }

    public void updatePublishers()
    {
        for (ObjectMap.Entry<String, Publisher> entry : publishers)
        {
            updatePublisher(entry.key);
        }
    }

    public void clear()
    {
        for (ObjectMap.Entry<String, Subscribers> entry : subscribers)
        {
            entry.value.clear();
        }

        subscribers.clear();
        publishers.clear();
    }
}
