package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ClientGeneratorLightsComponent;
import com.desertkun.brainout.content.components.SpriteComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FreeplayGeneratorData;
import com.desertkun.brainout.data.active.SpriteData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientGeneratorLightsComponent")
@ReflectAlias("data.components.ClientGeneratorLightsComponentData")
public class ClientGeneratorLightsComponentData extends Component<ClientGeneratorLightsComponent>
{
    private boolean working;
    private float workingTime;

    public ClientGeneratorLightsComponentData(
        ComponentObject componentObject,
        ClientGeneratorLightsComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    private FreeplayGeneratorData getGenerator()
    {
        return ((FreeplayGeneratorData) getComponentObject());
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        boolean w = getGenerator().isWorking();

        if (w != working)
        {
            working = w;

            if (!w)
            {
                enableLights(false);
            }
        }
        else
        {
            if (w)
            {
                float t = getGenerator().getWorkingTime();

                if (Math.abs(t - workingTime) > .5f)
                {
                    enableLights(t % 1.0f > 0.5f);

                    workingTime = t;
                }
            }
        }
    }

    private void enableLights(boolean b)
    {
        for (Map map : Map.All())
        {
            for (String id : getContentComponent().getIds())
            {
                ActiveData activeData = map.getActiveNameIndex().get(id);

                if (!(activeData instanceof SpriteData))
                    continue;

                SpriteData spriteData = ((SpriteData) activeData);

                SpriteComponentData spriteComponentData = spriteData.getComponent(SpriteComponentData.class);

                if (spriteComponentData == null)
                    continue;

                spriteData.spriteName = b ? getContentComponent().getOn() : getContentComponent().getOff();

                spriteComponentData.updateSprite();
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
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
