package com.desertkun.brainout.client.states;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.server.editor.MapListMsg;
import com.desertkun.brainout.events.MapListReceivedEvent;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.editor2.Editor2IntroMenu;
import com.desertkun.brainout.menu.impl.EmptyStateMenu;
import com.desertkun.brainout.mode.GameMode;

public class CSEmpty extends ControllerState
{
    private final GameMode.ID nextMode;

    public CSEmpty(GameMode.ID nextMode)
    {
        this.nextMode = nextMode;
    }

    @Override
    public ID getID()
    {
        return ID.empty;
    }

    @Override
    public void init()
    {
        BrainOutClient.MusicMng.stopMusic();
    }

    public GameMode.ID getNextMode()
    {
        return nextMode;
    }

    @Override
    public void release()
    {

    }

    @SuppressWarnings("unused")
    public boolean received(MapListMsg msg)
    {
        BrainOutClient.EventMgr.sendDelayedEvent(MapListReceivedEvent.obtain(msg.maps));

        return true;
    }

    public void pushMenu(GameState state)
    {
        state.popAll();

        Menu nextMenu;

        switch (nextMode)
        {
            case editor2:
            {
                nextMenu = new Editor2IntroMenu();
                break;
            }
            default:
            {
                nextMenu = new EmptyStateMenu();
                break;
            }
        }

        state.pushMenu(nextMenu);
    }
}
