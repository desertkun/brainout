package com.desertkun.brainout.events;

public class ErrorEvent extends Event
{
    public String errorText;

    @Override
    public ID getID()
    {
        return ID.error;
    }

    private Event init(String errorText)
    {
        this.errorText = errorText;

        return this;
    }

    public static Event obtain(String errorText)
    {
        ErrorEvent e = obtain(ErrorEvent.class);
        if (e == null) return null;
        return e.init(errorText);
    }

    @Override
    public void reset()
    {
        this.errorText = null;
    }
}
