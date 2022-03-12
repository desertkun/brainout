package com.desertkun.brainout.events;

import com.desertkun.brainout.client.states.CSGame;

public class FreePlayRadioEvent extends Event
{
    public String message;
    public int repeat;

    @Override
    public ID getID()
    {
        return ID.freePlayRadio;
    }

    private Event init(String message, int repeat)
    {
        this.message = message;
        this.repeat = repeat;

        return this;
    }

    public static Event obtain(String message, int repeat)
    {
        FreePlayRadioEvent e = obtain(FreePlayRadioEvent.class);
        if (e == null) return null;
        return e.init(message, repeat);
    }

    @Override
    public void reset()
    {
        this.message = null;
    }
}
