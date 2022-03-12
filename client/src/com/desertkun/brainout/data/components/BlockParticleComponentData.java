package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.BlockParticleComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BlockParticleComponent")
@ReflectAlias("data.components.BlockParticleComponentData")
public class BlockParticleComponentData extends Component<BlockParticleComponent>
{
    private EffectData effectData;

    public BlockParticleComponentData(ComponentObject componentObject, BlockParticleComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public void init()
    {
        super.init();

        ClientMap map = Map.Get(BlockData.CURRENT_DIMENSION, ClientMap.class);

        if (map == null)
            return;

        effectData = map.addEffect(getContentComponent().getParticleEffect(),
            new PointLaunchData(BlockData.CURRENT_X + 0.5f, BlockData.CURRENT_Y + 0.5f, 0,
                BlockData.CURRENT_DIMENSION));
    }

    @Override
    public void release()
    {
        super.release();

        ClientMap map = Map.Get(BlockData.CURRENT_DIMENSION, ClientMap.class);

        if (map == null)
            return;

        if (effectData != null)
        {
            effectData.release();
            effectData = null;
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

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
