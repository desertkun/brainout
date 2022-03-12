package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.ResourceDispenserComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ResourceDispenserComponent")
@ReflectAlias("data.components.ResourceDispenserComponentData")
public class ResourceDispenserComponentData<T extends ResourceDispenserComponent> extends Component<T>
{
    private final float period;
    private final float distance;
    private final ResourceDispenserComponent dispenser;
    private float timeToLive;
    private float timer;

    public ResourceDispenserComponentData(ComponentObject componentObject,
        T contentComponent)
    {
        super(componentObject, contentComponent);

        this.dispenser = contentComponent;

        this.period = contentComponent.getPeriod();
        this.distance = contentComponent.getDistance() * contentComponent.getDistance();
        this.timer = 0;
        this.timeToLive = contentComponent.getTimeToLive();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer += dt;

        if (timer >= period)
        {
            timer = 0;

            onUpdateDispenser();
        }

        timeToLive -= dt;

        if (timeToLive < 0)
        {
            BrainOut.EventMgr.sendDelayedEvent(getComponentObject(), DestroyEvent.obtain());
        }
    }

    public String getResourceName()
    {
        return null;
    }

    private void onUpdateDispenser()
    {
        if (BrainOutServer.Controller.isLobby())
            return;

        Map map = getMap();

        if (map == null)
            return;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        ActiveData activeData = ((ActiveData) getComponentObject());

        float x = activeData.getX(), y = activeData.getY();

        for (ActiveData other: map.getActivesForTag(Constants.ActiveTags.RESOURCE_RECEIVER, false))
        {
            if (other instanceof PlayerData)
            {
                float diffX = (other.getX() - x), diffY = (other.getY() - y);
                float dist = diffX * diffX + diffY * diffY;

                if (dist < distance)
                {
                    deliverResource(other);
                }
            }
        }
    }

    protected void deliverResource(ActiveData activeData)
    {

    }

    protected void resourceDelivered(ActiveData active, float amount)
    {
        if (dispenser.getEffect() != null)
        {
            BrainOutServer.Controller.getClients().sendUDP(
                new LaunchEffectMsg(active.getDimension(), active.getX(), active.getY(), dispenser.getEffect())
            );
        }

        if (getComponentObject() instanceof ActiveData)
        {
            ActiveData me = ((ActiveData) getComponentObject());

            int owner = me.getOwnerId();
            Client client = BrainOutServer.Controller.getClients().get(owner);

            if (client != null)
            {
                client.addStat("provided-" + getResourceName(), amount);

                if (active.getOwnerId() != owner)
                {
                    if (dispenser.getRewardOwner() != 0)
                    {
                        int toAward = dispenser.getRewardOwner();

                        client.addScore(toAward, true);
                        client.notify(NotifyAward.score, toAward,
                                NotifyReason.none, NotifyMethod.message, null);
                    }
                }
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
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
}
