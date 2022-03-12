package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.EntityReceived;
import com.desertkun.brainout.common.msg.server.ActiveReceivedConsumableMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.RandomJumpComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.ActivateInstrumentEvent;
import com.desertkun.brainout.events.AddImpulseEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.TimerTask;

@Reflect("RandomJumpComponent")
@ReflectAlias("data.components.RandomJumpComponentData")
public class RandomJumpComponentData extends Component<RandomJumpComponent>
{
    private final ActiveData activeData;

    public RandomJumpComponentData(ActiveData activeData,
                                   RandomJumpComponent component)
    {
        super(activeData, component);

        this.activeData = activeData;
    }

    @Override
    public void init()
    {
        super.init();

        scheduleJump();
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    private void scheduleJump()
    {
        BrainOutServer.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    jump(false);

                    scheduleJump();
                });
            }
        }, MathUtils.random(2000, 5000));
    }

    public void jump(boolean ignoreDead)
    {
        if (!ignoreDead && !activeData.isAlive())
            return;

        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy == null)
            return;

        float p = getContentComponent().getPower();

        phy.getSpeed().add(
            MathUtils.random(5.0f, 10.0f) * MathUtils.randomSign() * p,
            18.0f * p
        );

        ServerSimplePhysicsSyncComponentData
            sync = activeData.getComponent(ServerSimplePhysicsSyncComponentData.class);

        if (sync != null)
        {
            BrainOutServer.PostRunnable(() -> sync.trigger(false));
        }
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
}
