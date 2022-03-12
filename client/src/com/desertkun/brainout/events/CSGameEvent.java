package com.desertkun.brainout.events;

import com.desertkun.brainout.client.states.CSGame;

public class CSGameEvent extends Event
{
    public CSGame game;
    public CSGame.State state;

    @Override
    public ID getID()
    {
        return ID.csGame;
    }

    private Event init(CSGame game, CSGame.State state)
    {
        this.game = game;
        this.state = state;

        return this;
    }

    public static Event obtain(CSGame game, CSGame.State state)
    {
        CSGameEvent e = obtain(CSGameEvent.class);
        if (e == null) return null;
        return e.init(game, state);
    }

    @Override
    public void reset()
    {
        this.game = null;
        this.state = null;
    }
}
