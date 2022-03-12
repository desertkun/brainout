package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.components.RequestDropOffComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeRealization;
import com.desertkun.brainout.mode.ServerFreeRealization;

public class RequestDropOffComponentData extends Component<RequestDropOffComponent>
{
    private final ActiveData activeData;
    private float timer;

    public RequestDropOffComponentData(ActiveData activeData,
                                       RequestDropOffComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
        this.timer = contentComponent.getTime();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (timer > 0)
        {
            timer -= dt;

            if (timer <= 0)
            {
                BrainOutServer.PostRunnable(this::trigger);
            }
        }
    }

    private void trigger()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        GameModeRealization realization = gameMode.getRealization();

        if (!(realization instanceof ServerFreeRealization))
            return;

        ServerFreeRealization free = ((ServerFreeRealization) realization);

        Map map = getMap();

        if (map != null)
            free.requestDropOff(map, activeData.getX(), activeData.getY(), getContentComponent().getKind());
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
