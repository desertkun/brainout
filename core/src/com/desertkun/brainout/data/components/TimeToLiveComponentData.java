package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.TimeToLiveComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("TimeToLiveComponent")
@ReflectAlias("data.components.TimeToLiveComponentData")
public class TimeToLiveComponentData extends Component<TimeToLiveComponent> implements Json.Serializable
{
    private float time;

    public TimeToLiveComponentData(ComponentObject componentObject,
       TimeToLiveComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.time = contentComponent.getTime();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        time -= dt;

        if (time <= 0)
        {
            BrainOut.EventMgr.sendDelayedEvent(getComponentObject(), DestroyEvent.obtain());
        }
    }

    public float getTime()
    {
        return time;
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

    public void setTime(float value)
    {
        this.time = MathUtils.clamp(value, 0, getContentComponent().getTime());
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("tm", time);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        setTime(jsonData.getFloat("tm", time));
    }
}
