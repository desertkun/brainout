package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.BulletThrowableComponent;
import com.desertkun.brainout.content.components.ExplosiveComponent;
import com.desertkun.brainout.content.components.ThrowableAnimationComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ExplosiveComponent")
@ReflectAlias("data.components.ExplosiveComponentData")
public class ExplosiveComponentData extends Component<ExplosiveComponent>
{
    public ExplosiveComponentData(ComponentObject componentObject, ExplosiveComponent explosiveComponent)
    {
        super(componentObject, explosiveComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroy:
            {
                explode(((DestroyEvent) event).destroyer);

                break;
            }
        }

        return false;
    }

    private void explode(int destroyer)
    {
        int owner;
        float x, y;
        InstrumentInfo info;
        String dimension;
        Team team;

        if (getComponentObject() instanceof ThrowableActiveData)
        {
            ThrowableActiveData activeData = ((ThrowableActiveData) getComponentObject());

            owner = activeData.getOwnerId();
            x = activeData.getX();
            y = activeData.getY();
            info = activeData.getLaunchedBy();
            dimension = activeData.getDimension();
            team = activeData.getTeam();
        }
        else if (getComponentObject() instanceof BulletData)
        {
            BulletData bulletData = ((BulletData) getComponentObject());

            owner = bulletData.getOwnerId();
            x = bulletData.getX();
            y = bulletData.getY();
            info = bulletData.getInstrumentInfo();
            dimension = bulletData.getDimension();
            team = bulletData.getTeam();
        }
        else
        {
            return;
        }

        int amount = getContentComponent().getAmount();
        Bullet bullet = getContentComponent().getBullet();

        int ownerId = destroyer == -1 ? owner : destroyer;

        ServerMap map = Map.Get(dimension, ServerMap.class);
        if (map == null)
            return;

        BulletThrowableComponent throwableComponent = bullet.getComponent(BulletThrowableComponent.class);

        PointLaunchData launchData = new PointLaunchData(x, y, 0, dimension);

        if (throwableComponent != null)
        {

            ThrowableActive thr = throwableComponent.getThrowActive();

            for (int i = 0; i < amount; i++)
            {
                float angle = 360.0f * ((float) i / (float) amount);

                ThrowableActiveData activeData = thr.getData(map.getDimension());

                activeData.setPosition(launchData.getX(), launchData.getY());
                activeData.setOwnerId(owner);
                activeData.setAngle(angle);
                activeData.setLaunchedBy(info);
                activeData.setTeam(team);

                SimplePhysicsComponentData phy =
                    activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

                phy.getSpeed().set(
                    throwableComponent.getThrowPower() * MathUtils.cosDeg(angle),
                    throwableComponent.getThrowPower() * MathUtils.sinDeg(angle)
                );

                map.addActive(map.generateServerId(), activeData, true);
            }
        }
        else
        {
            for (int i = 0; i < amount; i++)
            {
                float angle = 360.0f * ((float)i / (float)amount);

                launchData.setAngle(angle);

                BulletData bulletData = bullet.getData(launchData, getContentComponent().getDamage(),
                        getComponentObject().getDimension());
                bulletData.setInstrumentInfo(info);
                bulletData.setOwnerId(ownerId);

                map.addBullet(bulletData);
            }
        }

        if (canLaunchEffect(dimension, x, y))
        {
            if (!getContentComponent().getEffect().isEmpty())
            {
                BrainOutServer.Controller.getClients().sendUDP(new LaunchEffectMsg(
                    dimension, x, y, getContentComponent().getEffect()));
            }
        }
        else
        {
            if (!getContentComponent().getEffect().isEmpty())
            {
                Client ownerClient = BrainOutServer.Controller.getClients().get(ownerId);

                if (ownerClient instanceof PlayerClient)
                {
                    ((PlayerClient) ownerClient).sendUDP(new LaunchEffectMsg(
                        dimension, x, y, getContentComponent().getEffect()));
                }
            }
        }
    }

    private boolean canLaunchEffect(String dimension, float x, float y)
    {
        ServerMap map = Map.Get(dimension, ServerMap.class);

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt((int)x, (int)y);

        return chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);
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
