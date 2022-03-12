package com.desertkun.brainout.menu.impl;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.NewOrderResultEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.gs.GameState;

abstract class NewOrderResultMenu extends WaitLoadingMenu implements EventReceiver
{
    public NewOrderResultMenu(String loadingTitle)
    {
        super(loadingTitle, false);
    }

    @Override
    public boolean popIfFocusOut()
    {
        return true;
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case newOrderResult:
            {
                NewOrderResultEvent e = ((NewOrderResultEvent) event);
                newOrderResult(e.success, e.reason);

                break;
            }

            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                switch (simpleEvent.getAction())
                {
                    case userProfileUpdated:
                    {
                        userProfileUpdated();

                        break;
                    }
                }
                break;
            }
        }

        return false;
    }

    protected abstract void userProfileUpdated();

    private void newOrderResult(boolean success, String reason)
    {
        GameState gs = getGameState();
        pop();

        if (!success)
        {
            if (reason == null || reason.equals(""))
            {
                gs.pushMenu(new NewOrderResultPopup(L.get("MENU_PURCHASE_ERROR")));
            }
            else
            {
                gs.pushMenu(new NewOrderResultPopup(L.get(reason)));
            }
        }
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.newOrderResult, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.newOrderResult, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
    }
}
