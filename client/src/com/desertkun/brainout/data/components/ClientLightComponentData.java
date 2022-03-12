package com.desertkun.brainout.data.components;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.LightEntity;
import com.desertkun.brainout.content.components.ClientLightComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.LightEntityData;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.LightData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.physics.PhysicChunk;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.PhysicChunkUpdatedEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientLightComponent")
@ReflectAlias("data.components.ClientLightComponentData")
public class ClientLightComponentData extends Component<ClientLightComponent>
{
    private final ActiveData lightData;
    private LightEntity lightEntity;
    private LightEntityData light;

    private static Filter SHADOW_FILTER = new Filter();

    static
    {
        SHADOW_FILTER.categoryBits = 1 << Constants.Physics.CATEGORY_LIGHT;
        SHADOW_FILTER.maskBits = ~(1 << Constants.Physics.CATEGORY_BELT | 1 << Constants.Physics.CATEGORY_RAGDOLL);
    }

    public ClientLightComponentData(ActiveData lightData,
                                    ClientLightComponent lightComponent)
    {
        super(lightData, lightComponent);

        this.lightData = lightData;
    }

    public LightEntityData getLight()
    {
        return light;
    }

    @Override
    public void init()
    {
        super.init();

        if (lightData instanceof LightData)
        {
            lightEntity = ((LightData) lightData).getLightEntity();
        }
        else
        {
            lightEntity = getContentComponent().getLightEntity();
        }

        light = new LightEntityData(lightEntity, lightData.getDimension())
        {
            @Override
            public float getX()
            {
                return lightData.getX();
            }

            @Override
            public float getY()
            {
                return lightData.getY();
            }
        };

        light.init();

        if (light.getLight() != null)
        {
            light.getLight().setContactFilter(SHADOW_FILTER);
        }

        BrainOut.EventMgr.subscribe(Event.ID.physicsUpdated, this);
    }

    private void update()
    {
        Light entity = light.getLight();
        if (entity != null)
        {
            if (lightData instanceof LightData)
            {
                Color color = ((LightData) lightData).getParsedColor();

                if (!BrainOutClient.ClientSett.hasSoftShadows())
                {
                    color.a *= 0.4f;
                }

                entity.setColor(color);
                entity.setDistance(((LightData) lightData).getDistance());
            }

            entity.setActive(lightData.isVisible());
        }

        light.update();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (light != null)
        {
            light.update();
        }
    }

    @Override
    public void release()
    {
        BrainOut.EventMgr.unsubscribe(Event.ID.physicsUpdated, this);

        super.release();

        if (light != null)
        {
            light.dispose();
            light = null;
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeAction:
            {
                ActiveActionEvent e = ((ActiveActionEvent) event);

                if (e.action == ActiveActionEvent.Action.updated)
                {
                    update();
                }

                break;
            }
            case physicsUpdated:
            {
                PhysicChunkUpdatedEvent e = ((PhysicChunkUpdatedEvent) event);

                if (light == null || light.getLight() == null)
                    return false;

                PhysicChunk chunk = e.physicChunk;

                int x = chunk.getX() + Constants.Physics.PHYSIC_BLOCK_SIZE / 2,
                    y = chunk.getY() + Constants.Physics.PHYSIC_BLOCK_SIZE / 2;


                if (Vector2.dst(x, y,
                    lightData.getX(), lightData.getY()) <= light.getLight().getDistance()
                        + Constants.Physics.PHYSIC_BLOCK_SIZE)
                {
                    update();
                }

                break;
            }
        }

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
