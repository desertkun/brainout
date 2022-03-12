package com.desertkun.brainout.data.parallax;

import com.desertkun.brainout.content.parallax.DynamicLayer;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.Watcher;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.parallax.DynamicLayerData")
public class DynamicLayerData extends StaticLayerData
{
    private final float flowX;
    private float flowedX;

    public DynamicLayerData(DynamicLayer layer, ParallaxData parallaxData, Map map)
    {
        super(layer, parallaxData, map);

        this.flowedX = 0;
        this.flowX = layer.getFlowX();
    }

    @Override
    public float getPlayerX(Watcher watcher)
    {
        return super.getPlayerX(watcher) + flowedX;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        flowedX += dt * flowX;
    }


}
