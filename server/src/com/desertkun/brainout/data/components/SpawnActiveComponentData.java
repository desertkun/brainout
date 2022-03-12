package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.SpawnActiveComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("SpawnActiveComponent")
@ReflectAlias("data.components.SpawnActiveComponentData")
public class SpawnActiveComponentData extends Component<SpawnActiveComponent>
{
    public SpawnActiveComponentData(ComponentObject componentObject, SpawnActiveComponent explosiveComponent)
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
                spawn(((DestroyEvent) event).destroyer);

                break;
            }
        }

        return false;
    }

    private void spawn(int destroyer)
    {
        int owner;
        float x, y, angle, speed;
        Team team;
        String dimension;
        InstrumentInfo instrumentInfo;

        if (getComponentObject() instanceof ThrowableActiveData)
        {
            instrumentInfo = ((ThrowableActiveData) getComponentObject()).getLaunchedBy();
        }
        else
        {
            instrumentInfo = null;
        }

        if (getComponentObject() instanceof ActiveData)
        {
            ActiveData activeData = ((ActiveData) getComponentObject());

            owner = activeData.getOwnerId();
            x = activeData.getX();
            y = activeData.getY();
            angle = activeData.getAngle();
            team = activeData.getTeam();
            dimension = activeData.getDimension();

            SimplePhysicsComponentData phy =
                    activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            speed = phy != null ? phy.getSpeed().len() : 0;
        }
        else if (getComponentObject() instanceof BulletData)
        {
            BulletData bulletData = ((BulletData) getComponentObject());

            owner = bulletData.getOwnerId();
            x = bulletData.getX();
            y = bulletData.getY();
            angle = bulletData.getAngle();
            dimension = bulletData.getDimension();

            Client client = BrainOutServer.Controller.getClients().get(owner);

            team = client != null ? client.getTeam() : null;
            speed = bulletData.getBullet().getSpeed();
        }
        else
        {
            return;
        }

        Map map = Map.Get(dimension);

        if (map == null)
            return;

        ActiveData activeData = getContentComponent().getActive().getData(dimension);

        activeData.setPosition(x, y);
        activeData.setOwnerId(owner);
        activeData.setAngle(0);
        activeData.setTeam(team);

        PassInstrumentInfoComponentData ii = activeData.getComponent(PassInstrumentInfoComponentData.class);
        if (ii != null)
        {
            ii.setInstrumentInfo(instrumentInfo);
        }

        SimplePhysicsComponentData phy =
            activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            speed *= getContentComponent().getSpeedCoef();

            phy.getSpeed().set(
                    speed * MathUtils.cosDeg(angle),
                    speed * MathUtils.sinDeg(angle)
            );
        }

        map.addActive(map.generateServerId(), activeData, true);

        if (!getContentComponent().getEffect().isEmpty())
        {
            BrainOutServer.Controller.getClients().sendUDP(new LaunchEffectMsg(
                dimension, x, y, getContentComponent().getEffect()));
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
