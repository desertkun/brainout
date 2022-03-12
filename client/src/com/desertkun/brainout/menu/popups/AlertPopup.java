package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.ArrayMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.menu.Popup;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class AlertPopup extends Popup implements EventReceiver
{
    public AlertPopup(String text)
    {
        super(text);

        init();
    }

    public String getOKText()
    {
        return L.get("MENU_OK");
    }

    private void init()
    {
        ArrayMap<String, PopupButtonStyle> buttons = new ArrayMap<>();

        buttons.put(getOKText(), new PopupButtonStyle(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                pop();
                ok();
            }
        }));

        setButtons(buttons);
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.gameController, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.gameController, this);
    }

    @Override
    public boolean escape()
    {
        ok();
        pop();

        return true;
    }

    public void ok()
    {

    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case beginLaunch:
                    {
                        escape();

                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }
}
