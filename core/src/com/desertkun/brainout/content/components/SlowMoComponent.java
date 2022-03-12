package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.lang.ref.WeakReference;

@Reflect("content.components.SlowMoComponent")
public class SlowMoComponent extends Component
{
    private final WeakReference<Map> map;
    private float speedFrom;
    private float speedTo;
    private float maxTime;
    private float time;
    private Interpolation interpolation;

    public SlowMoComponent(Map map, float speedFrom, float speedTo, float time, Interpolation interpolation)
    {
        super(null, null);

        this.map = new WeakReference<>(map);
        update(speedFrom, speedTo, time, interpolation);
    }

    public void update(float speedFrom, float speedTo, float time, Interpolation interpolation)
    {
        this.speedFrom = speedFrom;
        this.speedTo = speedTo;
        this.maxTime = time;
        this.time = 0;
        this.interpolation = interpolation;
    }

    public float getSpeedFrom()
    {
        return speedFrom;
    }

    public float getSpeedTo()
    {
        return speedTo;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        time += dt;

        float speed = interpolation.apply(speedFrom, speedTo, MathUtils.clamp(time / maxTime, 0.25f, 1.0f));

        Map map = this.map.get();

        if (map != null)
        {
            map.setSpeed(speed);

            if (time >= maxTime)
            {
                map.setSpeed(speedTo);
                map.getComponents().removeComponent(this);
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
