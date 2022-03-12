package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.BulletExplosiveComponent;
import com.desertkun.brainout.content.components.ExplosiveComponent;
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

@Reflect("BulletExplosiveComponent")
@ReflectAlias("data.components.BulletExplosiveComponentData")
public class BulletExplosiveComponentData extends Component<BulletExplosiveComponent>
{
    public BulletExplosiveComponentData(ComponentObject componentObject, BulletExplosiveComponent explosiveComponent)
    {
        super(componentObject, explosiveComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    private void explode(int destroyer)
    {
        int owner;
        float x, y;
        InstrumentInfo info;
        String dimension;

        if (getComponentObject() instanceof ThrowableActiveData)
        {
            ThrowableActiveData activeData = ((ThrowableActiveData) getComponentObject());

            owner = activeData.getOwnerId();
            x = activeData.getX();
            y = activeData.getY();
            info = activeData.getLaunchedBy();
            dimension = activeData.getDimension();
        }
        else if (getComponentObject() instanceof BulletData)
        {
            BulletData bulletData = ((BulletData) getComponentObject());

            owner = bulletData.getOwnerId();
            x = bulletData.getX();
            y = bulletData.getY();
            info = bulletData.getInstrumentInfo();
            dimension = bulletData.getDimension();
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

        PointLaunchData launchData = new PointLaunchData(x, y, 0, dimension);

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
    public void release()
    {
        super.release();

        if (getComponentObject() instanceof BulletData)
        {
            BulletData bulletData = ((BulletData) getComponentObject());

            explode(bulletData.getOwnerId());
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
