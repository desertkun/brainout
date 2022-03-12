package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.RadioactiveComponent;
import com.desertkun.brainout.content.components.WindComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("WindComponent")
@ReflectAlias("data.components.WindComponentData")
public class WindComponentData
        extends Component<WindComponent>
        implements WithTag, Json.Serializable
{
    private float movement;
    private float power;

    public WindComponentData(ComponentObject componentObject, WindComponent contentComponent)
    {
        super(componentObject, contentComponent);
        this.power = contentComponent.getPower();
    }

    public void setMovement(float movement)
    {
        this.movement = movement;
    }

    public float getMovement()
    {
        return movement;
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

    public float getDistance()
    {
        return getContentComponent().getDistance();
    }

    @Override
    public void update(float dt)
    {
        ActiveData activeData = ((ActiveData) getComponentObject());
        activeData.setX(activeData.getX() + dt * movement);
    }

    public float func(float x, float y)
    {
        ActiveData it = ((ActiveData) getComponentObject());
        float dist = Math.abs(it.getX() - x);
        float max = getContentComponent().getDistance();

        if (dist > max)
            return 0;

        return (1.0f - dist / max);
    }

    public float func(float x, float y, float coef)
    {
        ActiveData it = ((ActiveData) getComponentObject());
        float dist = Math.abs(it.getX() - x);
        float max = getContentComponent().getDistance() * coef;

        if (dist > max)
            return 0;

        return (1.0f - dist / max);
    }

    public float getPower()
    {
        return getContentComponent().getPower() * power;
    }

    public void setPower(float power)
    {
        this.power = power;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.WIND);
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("pow", power);
        json.writeValue("mov", movement);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        power = jsonData.getFloat("pow", power);
        movement = jsonData.getFloat("mov", movement);
    }
}
