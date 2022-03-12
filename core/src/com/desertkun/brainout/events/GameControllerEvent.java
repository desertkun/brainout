package com.desertkun.brainout.events;

import com.badlogic.gdx.math.Vector2;

public class GameControllerEvent extends Event
{
    public enum Action
    {
        beginLaunch,
        endLaunch,

        openChat,
        openTeamChat,
        openConsole,
        openPlayerList,
        closePlayerList,

        move,
        aim,
        absoluteAim,

        switchSource,
        switchShootMode,
        selectSource,
        reload,
        unload,
        switchWeapon,

        dropInstrument,
        dropAmmo,
        changeTeam,

        back,
        select,

        beginSit,
        endSit,

        squat,

        beginRun,
        endRun,

        voiceChatBegin,
        voiceChatEnd,

        switchZoom,
        hideInterface,

        beginLaunchSecondary,
        endLaunchSecondary,
        custom,

        activate,
        freePlayFriends
    }

    public Action action;
    public Vector2 data;
    public int num;
    public String string;
    public boolean flag;

    @Override
    public ID getID()
    {
        return ID.gameController;
    }

    private Event init(Action action, boolean flag)
    {
        this.action = action;
        this.flag = flag;

        return this;
    }

    private Event init(Action action, Vector2 data)
    {
        this.action = action;
        this.data = data;

        return this;
    }

    private Event init(Action action, Vector2 data, boolean flag)
    {
        this.action = action;
        this.data = data;
        this.flag = flag;

        return this;
    }

    private Event init(Action action, int num)
    {
        this.action = action;
        this.num = num;

        return this;
    }

    private Event init(Action action, String string)
    {
        this.action = action;
        this.num = 0;
        this.string = string;

        return this;
    }

    private Event init(Action action)
    {
        this.action = action;
        this.num = 0;
        this.string = null;

        return this;
    }

    public static Event obtain(Action action, boolean flag)
    {
        GameControllerEvent e = obtain(GameControllerEvent.class);
        if (e == null) return null;
        return e.init(action, flag);
    }

    public static Event obtain(Action action, Vector2 data)
    {
        GameControllerEvent e = obtain(GameControllerEvent.class);
        if (e == null) return null;
        return e.init(action, data);
    }

    public static Event obtain(Action action, Vector2 data, boolean flag)
    {
        GameControllerEvent e = obtain(GameControllerEvent.class);
        if (e == null) return null;
        return e.init(action, data, flag);
    }

    public static Event obtain(Action action, int num)
    {
        GameControllerEvent e = obtain(GameControllerEvent.class);
        if (e == null) return null;
        return e.init(action, num);
    }

    public static Event obtain(Action action, String string)
    {
        GameControllerEvent e = obtain(GameControllerEvent.class);
        if (e == null) return null;
        return e.init(action, string);
    }

    public static Event obtain(Action action)
    {
        GameControllerEvent e = obtain(GameControllerEvent.class);
        if (e == null) return null;
        return e.init(action);
    }

    @Override
    public void reset()
    {
        this.action = null;
        this.data = null;
        this.num = 0;
    }
}
