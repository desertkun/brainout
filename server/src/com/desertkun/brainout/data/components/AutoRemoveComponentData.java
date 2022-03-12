package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.components.AutoRemoveComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("AutoRemoveComponent")
@ReflectAlias("data.components.AutoRemoveComponentData")
public class AutoRemoveComponentData extends Component<AutoRemoveComponent>
{
    private final ActiveData activeData;
    private final AutoRemoveComponent.Kind kind;
    private float cnt;

    public AutoRemoveComponentData(ActiveData activeData, AutoRemoveComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
        this.kind = contentComponent.getKind();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void update(float dt)
    {
        cnt -= dt;

        if (cnt > 0)
            return;

        cnt = 0.2f;

        switch (kind)
        {
            case XLessThanZero:
            {
                if (activeData.getX() < 0)
                {
                    trigger();
                }

                break;
            }
        }
    }

    private void trigger()
    {
        BrainOutServer.PostRunnable(this::remove);
    }

    private void remove()
    {
        BrainOut.EventMgr.sendEvent(activeData, DestroyEvent.obtain());
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
