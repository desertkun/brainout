package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.EffectComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("EffectComponent")
@ReflectAlias("data.components.EffectComponentData")
public class EffectComponentData extends Component<EffectComponent>
{
    private final ActiveData activeData;

    private Array<EffectData> res;

    public EffectComponentData(ActiveData activeData, EffectComponent effectComponent)
    {
        super(activeData, effectComponent);

        this.activeData = activeData;
        this.res = new Array<>();
    }

    @Override
    public void init()
    {
        super.init();


        LaunchData launchData = new LaunchData()
        {
            @Override
            public float getX()
            {
                return activeData.getX();
            }

            @Override
            public float getY()
            {
                return activeData.getY();
            }

            @Override
            public float getAngle()
            {
                return activeData.getAngle();
            }

            @Override
            public String getDimension()
            {
                return activeData.getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        };

        getContentComponent().getEffectSet().launchEffects(launchData, res);
    }

    @Override
    public void release()
    {
        super.release();

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        for (EffectData effectData: res)
        {
            map.removeEffect(effectData);
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
        return false;
    }
}
