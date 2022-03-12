package com.desertkun.brainout.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.GameControllerEvent;

public abstract class GameController extends InputAdapter
{
    protected ControllerMode controllerMode;

    public enum ControllerMode
    {
        action,
        actionWithNoMouseLocking,
        move,
        clientList,
        disabled,
        disabledKeys,
        editor
    }

    public GameController()
    {
        this.controllerMode = ControllerMode.disabled;
    }

    public abstract void update(float dt);
    public void render() {}
    public void init() {}

    public boolean sendEvent(Event event)
    {
        return BrainOut.EventMgr.sendEvent(event);
    }

    public void sendDelayedEvent(Event event)
    {
        BrainOut.EventMgr.sendDelayedEvent(event);
    }

    public ControllerMode getControllerMode()
    {
        return controllerMode;
    }

    public void setControllerMode(ControllerMode controllerMode)
    {
        this.controllerMode = controllerMode;
    }

    public void clear()
    {
        setControllerMode(ControllerMode.disabled);
    }

    public void reset()
    {

    }
}
