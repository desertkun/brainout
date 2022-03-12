package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ServerActiveMoveMsg;
import com.desertkun.brainout.content.components.ServerSimplePhysicsSyncComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerSimplePhysicsSyncComponent")
@ReflectAlias("data.components.ServerSimplePhysicsSyncComponentData")
public class ServerSimplePhysicsSyncComponentData extends Component<ServerSimplePhysicsSyncComponent>
{
    private final ActiveData activeData;
    private float closeSync, farSync;

    public ServerSimplePhysicsSyncComponentData(ActiveData activeData,
                                                ServerSimplePhysicsSyncComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
        this.closeSync = 0;
        this.farSync = 0;
    }

    @Override
    public void update(float dt)
    {
        closeSync -= dt;

        if (closeSync <= 0)
        {
            closeSync = 0.25f;

            trigger(true);
        }

        if (farSync <= 0)
        {
            farSync = 5.0f;

            trigger(false);
        }
    }

    public void trigger(boolean close)
    {
        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy == null)
            return;

        Vector2 speed = phy.getSpeed();

        ServerActiveMoveMsg msg = new ServerActiveMoveMsg(
                activeData.getId(), activeData.getX(), activeData.getY(),
                speed.x, speed.y, activeData.getAngle(), activeData.getDimension());

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (!(client instanceof PlayerClient))
                continue;

            PlayerClient playerClient = ((PlayerClient) client);

            if (!activeData.getDimension().equals(playerClient.getLastKnownDimension()))
                continue;

            Vector2 p = playerClient.getLastKnownPosition();

            float dst = close ? 32 : 128;

            if (p.dst2(activeData.getX(), activeData.getY()) > dst * dst)
                continue;

            playerClient.sendUDP(msg);
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
