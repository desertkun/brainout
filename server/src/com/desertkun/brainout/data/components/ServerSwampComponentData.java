package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.ServerSwampComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerSwampComponent")
@ReflectAlias("data.components.ServerSwampComponentData")
public class ServerSwampComponentData extends Component<ServerSwampComponent>
{
    private final BlockData blockData;
    private final String activateEffect;
    private float timer;

    public ServerSwampComponentData(BlockData blockData, ServerSwampComponent contentComponent)
    {
        super(blockData, contentComponent);

        this.blockData = blockData;
        this.activateEffect = contentComponent.getActivateEffect();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.125f;

        Map map = getMap();

        if (map == null)
            return;

        for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
        {
            SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (phy == null)
                continue;

            int pX = ((int) activeData.getX());
            int pY = ((int) (activeData.getY() - phy.getHalfSize().y + 0.5f));

            if (BlockData.CURRENT_X == pX && BlockData.CURRENT_Y == pY)
            {
                detect(activeData);
            }
        }
    }

    private void detect(ActiveData activeData)
    {
        int ownerId = activeData.getOwnerId();

        FreeplayPlayerComponentData fp = activeData.getComponent(FreeplayPlayerComponentData.class);
        if (fp == null || fp.isSwamp())
            return;

        Client client = BrainOutServer.Controller.getClients().get(ownerId);
        if (client != null)
        {
            HealthComponentData h = activeData.getComponent(HealthComponentData.class);
            if (h != null && h.isGod())
            {
                return;
            }

            BrainOutServer.Controller.getClients().sendUDP(
                new LaunchEffectMsg(activeData.getDimension(), activeData.getX(), activeData.getY(),
                    activateEffect));

            client.enablePlayer(false);

            ServerPlayerControllerComponentData ctl =
                activeData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);
            if (ctl != null)
            {
                ctl.setMoveDirection(0, 0);
                ctl.sendPlayerDataTCPIncludingOwner();
            }

            SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);
            if (phy != null)
            {
                phy.setEnabled(false);
                phy.updated(activeData);
            }

            fp.setSwamp(true);
            fp.sync();
        }
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
