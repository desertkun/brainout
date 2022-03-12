package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ActiveDamageMsg;
import com.desertkun.brainout.content.components.HealthComponent;
import com.desertkun.brainout.content.components.ServerHealthComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.events.DamagedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerHealthComponent")
@ReflectAlias("data.components.ServerHealthComponentData")
public class ServerHealthComponentData extends Component<ServerHealthComponent>
{
    public ServerHealthComponentData(ComponentObject componentObject,
                                     ServerHealthComponent contentComponent)
    {
        super(componentObject, contentComponent);
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
            case damaged:
            {
                DamagedEvent damagedEvent = ((DamagedEvent) event);

                damaged(damagedEvent);

                break;
            }
        }

        return false;
    }

    private void damaged(DamagedEvent event)
    {
        if (event.data instanceof ActiveData)
        {
            ActiveData activeData = ((ActiveData) event.data);

            if (activeData instanceof PlayerData)
            {
                Client owner = BrainOutServer.Controller.getClients().get(activeData.getOwnerId());

                if (owner != null)
                {
                    GameMode mode = BrainOutServer.Controller.getGameMode();

                    if (mode != null)
                    {
                        ((ServerRealization) mode.getRealization()).onClientDamaged(
                            owner, ((PlayerData) activeData), event.damageKind);
                    }
                }
            }

            Map map = activeData.getMap();

            if (map == null)
                return;

            ChunkData chunk = map.getChunkAt((int)activeData.getX(), (int)activeData.getY());

            if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                return;

            boolean syncToOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

            if (syncToOthers)
            {
                ActiveDamageMsg msg = new ActiveDamageMsg(
                    activeData,
                    event.health,
                    event.x, event.y, event.angle,
                    event.content, event.damageKind);

                if (activeData instanceof PlayerData)
                {
                    Client owner = BrainOutServer.Controller.getClients().get(activeData.getOwnerId());

                    if (owner instanceof PlayerClient)
                    {
                        ServerPlayerControllerComponentData spc = activeData.getComponent(ServerPlayerControllerComponentData.class);
                        if (spc != null)
                        {
                            BrainOutServer.Controller.getClients().sendUDP(msg,
                                (client) -> client == owner || spc.validPlayer(client, 1));
                        }
                        return;
                    }
                }

                BrainOutServer.Controller.getClients().sendUDP(msg);
            }
            else
            {
                Client owner = BrainOutServer.Controller.getClients().get(activeData.getOwnerId());

                if (owner instanceof PlayerClient)
                {
                    ((PlayerClient) owner).sendUDP(
                        new ActiveDamageMsg(
                            activeData,
                            event.health,
                            event.x, event.y, event.angle,
                            event.content, event.damageKind));
                }
            }
        }
    }
}
