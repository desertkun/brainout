package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.components.ActiveAddStatOnDestroyComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ActiveAddStatOnDestroyComponent")
@ReflectAlias("data.components.ActiveAddStatOnDestroyComponentData")
public class ActiveAddStatOnDestroyComponentData extends Component<ActiveAddStatOnDestroyComponent>
{
    private final ActiveData activeData;

    public ActiveAddStatOnDestroyComponentData(
            ActiveData activeData, ActiveAddStatOnDestroyComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroy:
            {
                destroy();

                break;
            }
        }

        return false;
    }

    private void destroy()
    {
        ActiveData.LastHitInfo lastHit = activeData.getLastHitInfo();

        if (lastHit == null)
            return;

        Client killer = BrainOutServer.Controller.getClients().get(lastHit.hitterId);

        if (killer == null)
            return;

        GameMode mode = BrainOutServer.Controller.getGameMode();

        if (mode == null)
            return;

        switch (mode.getID())
        {
            case editor:
            case editor2:
            case free:
            case lobby:
            {
                return;
            }
        }

        killer.addStat(getContentComponent().getStat(), 1);
    }
}
