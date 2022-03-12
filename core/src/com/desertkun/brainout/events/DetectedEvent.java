package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;

public class DetectedEvent extends Event
{
    public String detectClass;
    public ActiveData detected;
    public EventKind eventKind;

    public enum EventKind
    {
        enter,
        leave
    }

    @Override
    public ID getID()
    {
        return ID.detected;
    }

    private Event init(String detectClass, ActiveData detected, EventKind eventKind)
    {
        this.detectClass = detectClass;
        this.detected = detected;
        this.eventKind = eventKind;

        return this;
    }

    public static Event obtain(String detectClass, ActiveData detected, EventKind eventKind)
    {
        DetectedEvent e = obtain(DetectedEvent.class);
        if (e == null) return null;
        return e.init(detectClass, detected, eventKind);
    }

    @Override
    public void reset()
    {
        this.detectClass = null;
        this.detected = null;
        this.eventKind = null;
    }
}
