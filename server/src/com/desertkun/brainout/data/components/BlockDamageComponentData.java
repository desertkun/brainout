package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.BlockDamageComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BlockDamageComponent")
@ReflectAlias("data.components.BlockDamageComponentData")
public class BlockDamageComponentData extends Component<BlockDamageComponent>
{
    private final BlockData blockData;
    private float timer;

    public BlockDamageComponentData(BlockData blockData,
                                    BlockDamageComponent blockDamageComponent)
    {
        super(blockData, blockDamageComponent);

        this.blockData = blockData;

        this.timer = 0;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;

        if (timer <= 0)
        {
            timer = getContentComponent().getPeriod();

            damage();
        }
    }

    private void damage()
    {
        Map map = getMap();

        if (map == null)
            return;

        for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.WITH_HEALTH, false))
        {
            HealthComponentData pcd = activeData.getComponent(HealthComponentData.class);

            if (pcd != null)
            {
                int pX = ((int) activeData.getX());
                int pY = ((int) activeData.getY());

                if (BlockData.CURRENT_X == pX && BlockData.CURRENT_Y == pY)
                {
                    pcd.damage((DamageEvent)DamageEvent.obtain(getContentComponent().getDamage(),
                            -1, null, null,
                            activeData.getX(), activeData.getY(), -1, Constants.Damage.DAMAGE_HIT));

                    pcd.updated(activeData);
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
