package com.desertkun.brainout.menu.impl;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.SimpleEvent;

public class WaitProfileMenu extends WaitLoadingMenu implements EventReceiver
{
    private final Runnable received;

    public WaitProfileMenu(Runnable received)
    {
        super("");

        this.received = received;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent ev = ((SimpleEvent) event);

                if (ev.getAction() == SimpleEvent.Action.userProfileUpdated)
                {
                    updated();
                }

                break;
            }
        }

        return false;
    }

    private void updated()
    {
        pop();
        received.run();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
