package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.HitConfirmMsg;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.TraceEffectComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("TraceEffectComponent")
@ReflectAlias("data.components.TraceEffectComponentData")
public class TraceEffectComponentData extends Component<TraceEffectComponent>
{
    private static Vector2 tmp = new Vector2();

    public TraceEffectComponentData(ComponentObject componentObject,
                                    TraceEffectComponent traceEffectComponent)
    {
        super(componentObject, traceEffectComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroy:
            {
                launchEffect();

                break;
            }
        }

        return false;
    }

    private void launchEffect()
    {
        ActiveData me = ((ActiveData) getComponentObject());

        float x = me.getX(), y = me.getY();

        Map map = me.getMap();

        if (map == null)
            return;

        ChunkData chunk = map.getChunkAt((int)me.getX(), (int)me.getY());

        if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
            return;

        boolean syncToOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

        boolean gotOne = false;

        for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
        {

            if (activeData instanceof PlayerData && activeData.getOwnerId() != -1)
            {
                if (!syncToOthers && activeData.getOwnerId() != me.getOwnerId())
                    continue;

                tmp.set(activeData.getX(), activeData.getY());
                tmp.sub(x, y);

                boolean v = tmp.len() < getContentComponent().getDistance() && (!map.trace(x, y,
                    Constants.Layers.BLOCK_LAYER_FOREGROUND, tmp.angleDeg(), tmp.len(), tmp));

                int owner = activeData.getOwnerId();
                Client client = BrainOutServer.Controller.getClients().get(owner);

                if (client instanceof PlayerClient)
                {
                    if (v)
                    {
                        gotOne = true;
                    }

                    ((PlayerClient) client).sendTCP(new LaunchEffectMsg(
                        activeData.getDimension(), x, y, getContentComponent().getEffect(v)));
                }
            }
        }

        if (gotOne && me.getOwnerId() != -1)
        {
            Client owner = BrainOutServer.Controller.getClients().get(me.getOwnerId());

            if (owner instanceof PlayerClient)
            {
                ((PlayerClient) owner).sendUDP(new HitConfirmMsg(null, me.getDimensionId(), me.getId(), x, y, 0));
            }
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
