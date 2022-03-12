package com.desertkun.brainout.events;

import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.states.ControllerState;

public class ClientControllerEvent extends Event
{
    public ClientController controller;
    public ControllerState state;

    @Override
    public ID getID()
    {
        return ID.controller;
    }

    public <T extends ControllerState> T get(Class<T> tClass)
    {
        return (T)state;
    }

    private Event init(ClientController controller, ControllerState state)
    {
        this.controller = controller;
        this.state = state;

        return this;
    }

    public static Event obtain(ClientController controller, ControllerState state)
    {
        ClientControllerEvent e = obtain(ClientControllerEvent.class);
        if (e == null) return null;
        return e.init(controller, state);
    }

    @Override
    public void reset()
    {
        this.controller = null;
        this.state = null;
    }
}
