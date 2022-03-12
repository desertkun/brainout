package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ServerSmokeComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.TimerTask;

@Reflect("ServerSmokeComponent")
@ReflectAlias("data.components.ServerSmokeComponentData")
public class ServerSmokeComponentData extends Component<ServerSmokeComponent>
{
    private float power;
    private float time;

    public ServerSmokeComponentData(ComponentObject componentObject, ServerSmokeComponent contentComponent)
    {
        super(componentObject, contentComponent);

        power = 4;
        time = 0;
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

    public void setTime(float time)
    {
        this.time = time;
    }

    @Override
    public void init()
    {
        super.init();

        final Map map = Map.Get(BlockData.CURRENT_DIMENSION);
        if (map == null)
            return;

        BlockData blockData = ((BlockData) getComponentObject());

        if (blockData == null)
            return;

        final int blockX = BlockData.CURRENT_X, blockY = BlockData.CURRENT_Y,
            blockLayer = BlockData.CURRENT_LAYER;

        checkBlocks(map, blockData, blockX, blockY);

        BrainOutServer.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() -> BrainOutServer.PostRunnable(() ->
                    map.setBlock(blockX, blockY, null, blockLayer, false)));
            }
        }, (long)(time * 1000.f));
    }

    private void checkBlocks(Map map, BlockData blockData, int blockX, int blockY)
    {
        if (power > 0.35f)
        {
            checkBlock(map, blockData, blockX + 1, blockY + 1, 0.35f);
            checkBlock(map, blockData, blockX - 1, blockY + 1, 0.35f);
            checkBlock(map, blockData, blockX + 1, blockY - 1, 0.35f);
            checkBlock(map, blockData, blockX - 1, blockY - 1, 0.35f);
        }

        if (power > 0.25f)
        {
            checkBlock(map, blockData, blockX - 1, blockY, 0.25f);
            checkBlock(map, blockData, blockX + 1, blockY, 0.25f);
            checkBlock(map, blockData, blockX, blockY - 1, 0.25f);
            checkBlock(map, blockData, blockX, blockY + 1, 0.25f);
        }
    }

    private void checkBlock(Map map, BlockData blockData, int x, int y, float loss)
    {
        final float powerPass = power - loss;

        BlockData check = map.getBlock(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

        if (check instanceof ConcreteBD)
            return;

        BlockData blockAt = map.getBlock(x, y, Constants.Layers.BLOCK_LAYER_UPPER);

        if (blockAt == null)
        {
            BlockData newBlock = blockData.getCreator().getBlock();

            ServerSmokeComponentData smoke = newBlock.getComponent(ServerSmokeComponentData.class);

            if (smoke != null)
            {
                smoke.setTime(time);
                smoke.power = powerPass;
            }

            map.setBlock(x, y, newBlock, Constants.Layers.BLOCK_LAYER_UPPER, false);

            BlockData.CURRENT_X = x; BlockData.CURRENT_Y = y;

            newBlock.init();
        }
        else
        {
            ServerSmokeComponentData smoke = blockAt.getComponent(ServerSmokeComponentData.class);

            if (smoke != null)
            {
                if (smoke.power < powerPass)
                {
                    smoke.power = powerPass;
                }
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
