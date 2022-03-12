package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.ComponentUpdatedEvent;
import com.esotericsoftware.minlog.Log;


public abstract class ActiveComponent<T extends ContentComponent> extends Component<T>
    implements Json.Serializable
{
    private final ActiveData activeData;

    public ActiveComponent(ActiveData activeData, T contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
    }

    public ActiveData getActiveData()
    {
        return activeData;
    }

    public void updated()
    {
        BrainOut.EventMgr.sendDelayedEvent(ComponentUpdatedEvent.obtain(this, activeData));

        if (Log.INFO) Log.info("Component " + getComponentClass().getCanonicalName() + " updated!");
    }
}
