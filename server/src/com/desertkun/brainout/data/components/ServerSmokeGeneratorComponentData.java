package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.ServerSmokeGeneratorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("ServerSmokeGeneratorComponent")
@ReflectAlias("data.components.ServerSmokeGeneratorComponentData")
public class ServerSmokeGeneratorComponentData extends Component<ServerSmokeGeneratorComponent>
{
    private float timer;
    private boolean active;
    private Vector2 prevPos;

    public ServerSmokeGeneratorComponentData(ComponentObject componentObject, ServerSmokeGeneratorComponent contentComponent)
    {
        super(componentObject, contentComponent);

        timer = 0;
        active = false;
        prevPos = new Vector2();
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

        ActiveData activeData = ((ActiveData) getComponentObject());
        Map map = activeData.getMap();
        if (map == null)
            return;

        if (!active)
        {
            timer += dt;

            if (timer > getContentComponent().getActivateTime())
            {
                SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

                if (phy.hasAnyContact()) {
                    if (prevPos.dst(activeData.getX(), activeData.getY()) < 0.01f) {
                        timer = 0.5f;
                        active = true;

                        BrainOutServer.Controller.getClients().sendUDP(
                                new LaunchEffectMsg(activeData.getDimension(), activeData.getX(), activeData.getY(),
                                        getContentComponent().getActivateEffect()));

                        generate();
                    }
                }
            }

            prevPos.set(activeData.getX(), activeData.getY());
        }


    }

    private static final int[] INDEXES_CHECK = new int[]{
        0, 0,
        -1, 0,
        1, 0,
        0, -1,
        0, 1,
        1, 1,
        -1, 1,
        1, -1,
        -1, -1,
        -2, -2,
        -2, 2,
        2, -2,
        2, 2,
        0, 2,
        0, -2,
        2, 0,
        -2, 0
    };

    private void generate()
    {
        ActiveData activeData = ((ActiveData) getComponentObject());
        Map map = activeData.getMap();
        if (map == null)
            return;

        TimeToLiveComponentData ttl = activeData.getComponent(TimeToLiveComponentData.class);

        int blockX = (int)activeData.getX(), blockY = (int)activeData.getY();

        for (int i = 0, t = INDEXES_CHECK.length; i < t; i += 2)
        {
            int x = blockX + INDEXES_CHECK[i], y = blockY + INDEXES_CHECK[i + 1];

            BlockData check = map.getBlock(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            if (check instanceof ConcreteBD)
                continue;

            BlockData at = map.getBlock(x, y, Constants.Layers.BLOCK_LAYER_UPPER);

            if (at == null)
            {
                BlockData newBlock = getContentComponent().getBlock().getBlock();
                map.setBlock(
                    x, y, newBlock,
                    Constants.Layers.BLOCK_LAYER_UPPER, false
                );

                BlockData.CURRENT_X = x; BlockData.CURRENT_Y = y;
                BlockData.CURRENT_LAYER = Constants.Layers.BLOCK_LAYER_UPPER;
                BlockData.CURRENT_DIMENSION = activeData.getDimension();

                ServerSmokeComponentData smoke = newBlock.getComponent(ServerSmokeComponentData.class);

                if (smoke != null)
                {
                    smoke.setTime(ttl.getTime());
                }

                newBlock.init();

                break;
            }
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
