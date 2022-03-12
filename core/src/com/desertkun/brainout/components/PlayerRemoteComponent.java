package com.desertkun.brainout.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.HookInstrumentEvent;
import com.desertkun.brainout.events.ResetInstrumentEvent;
import com.desertkun.brainout.events.SetInstrumentEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PlayerRemoteComponent")
public class PlayerRemoteComponent extends Component implements Json.Serializable
{
    private final PlayerData playerData;

    private InstrumentData currentInstrument;
    private InstrumentData hookedInstrument;

    public PlayerRemoteComponent(ComponentObject componentObject)
    {
        this((PlayerData)componentObject);
    }

    public PlayerRemoteComponent(PlayerData playerData)
    {
        super(playerData, null);

        this.playerData = playerData;
        this.currentInstrument = null;
        this.hookedInstrument = null;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    public InstrumentData getCurrentInstrument()
    {
        return currentInstrument;
    }

    public InstrumentData getHookedInstrument()
    {
        return hookedInstrument;
    }

    public void updateInstrument()
    {
        if (getCurrentInstrument() != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(playerData, SetInstrumentEvent.obtain(getCurrentInstrument(), playerData));
        }
        else
        {
            BrainOut.EventMgr.sendDelayedEvent(playerData, ResetInstrumentEvent.obtain(playerData));
        }
    }

    public void updateHookedInstrument()
    {
        InstrumentData hooked = getHookedInstrument();

        if (hooked == null)
            return;

        BrainOut.EventMgr.sendDelayedEvent(hooked, HookInstrumentEvent.obtain(hooked, playerData));

        playerData.setHookedInstrument(hookedInstrument);
    }

    public void setCurrentInstrument(InstrumentData currentInstrument)
    {
        this.currentInstrument = currentInstrument;
        if (currentInstrument != null)
        {
            currentInstrument.setOwner(playerData);
        }

        updateInstrument();
    }

    public void setHookedInstrument(InstrumentData hookedInstrument)
    {
        if (this.hookedInstrument == hookedInstrument)
            return;

        if (this.hookedInstrument != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(
                this.hookedInstrument, HookInstrumentEvent.obtain(null, playerData));
        }

        this.hookedInstrument = hookedInstrument;

        if (hookedInstrument != null)
        {
            hookedInstrument.setOwner(playerData);
        }

        updateHookedInstrument();
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("instrument", currentInstrument);

        if (hookedInstrument != null)
        {
            json.writeValue("hooked", hookedInstrument);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        Map map = getMap();

        if (map == null)
            return;

        if (jsonData.has("instrument"))
        {
            this.currentInstrument = map.newInstrumentData(json, jsonData.get("instrument"));

            if (currentInstrument != null)
            {
                currentInstrument.setOwner(playerData);
            }
        }

        if (jsonData.has("hooked"))
        {
            this.hookedInstrument = map.newInstrumentData(json, jsonData.get("hooked"));

            if (hookedInstrument != null)
            {
                hookedInstrument.setOwner(playerData);
            }
        }
        else
        {
            if (hookedInstrument != null)
            {
                hookedInstrument.release();
                hookedInstrument = null;
            }
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (currentInstrument != null)
        {
            currentInstrument.release();
        }

        if (hookedInstrument != null)
        {
            hookedInstrument.release();
        }
    }

    @Override
    public void init()
    {
        super.init();

        if (currentInstrument != null)
        {
            currentInstrument.init();
        }

        if (hookedInstrument != null)
        {
            hookedInstrument.init();
        }

        updateInstrument();
        updateHookedInstrument();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }
}
