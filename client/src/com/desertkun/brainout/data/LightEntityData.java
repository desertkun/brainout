package com.desertkun.brainout.data;

import box2dLight.ChainLight;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.PositionalLight;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.LightEntity;

public abstract class LightEntityData implements Disposable
{
    private final String dimension;
    private Light light;
    private final LightEntity entity;
    private boolean inited;

    public LightEntityData(LightEntity entity, String dimension)
    {
        this.entity = entity;
        this.dimension = dimension;
    }

    public abstract float getX();
    public abstract float getY();

    public Light getLight()
    {
        return light;
    }

    public void init()
    {
        this.light = newLight();

        if (light != null)
        {

            if (entity.isStatic())
            {
                light.setStaticLight(true);
            }

            if (entity.getRays() == 0 || entity.isxRay())
            {
                light.setXray(true);
            }

            light.setSoft(entity.getSoft() > 0);
            light.setSoftnessLength(entity.getSoft());

            inited = true;
        }
    }

    private Light newLight()
    {
        ClientMap map = Map.Get(dimension, ClientMap.class);

        if (map == null)
            return null;

        return map.getLights() != null ? new PointLight(map.getLights(), entity.getRays(), entity.getColor(),
                entity.getDistance(), getX(), getY()) : null;
    }

    public void update()
    {
        if (light != null)
        {
            light.setPosition(getX(), getY());
        }
    }

    @Override
    public void dispose()
    {
        if (inited)
        {
            inited = false;

            light.remove(true);
        }
    }

    public void setActive(boolean active)
    {
        if (light != null)
        {
            light.setActive(active);
        }
    }
}
