package com.desertkun.brainout.events;

public class PopupEvent extends Event
{
    public String title;
    public String data;

    @Override
    public ID getID()
    {
        return ID.popup;
    }

    private Event init(String title, String data)
    {
        this.title = title;
        this.data = data;

        return this;
    }

    public static Event obtain(String title, String data)
    {
        PopupEvent e = obtain(PopupEvent.class);
        if (e == null) return null;
        return e.init(title, data);
    }

    @Override
    public void reset()
    {
        this.title = null;
        this.data = null;
    }
}
