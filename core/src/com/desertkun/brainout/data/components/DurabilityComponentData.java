package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.components.DurabilityComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("DurabilityComponent")
@ReflectAlias("data.components.DurabilityComponentData")
public class DurabilityComponentData<T extends DurabilityComponent> extends Component<T> implements Json.Serializable
{
    private float durability;

    public DurabilityComponentData(ComponentObject componentObject,
                                   T contentComponent)
    {
        super(componentObject, contentComponent);

        this.durability = contentComponent.getDurability();
    }

    public float getDurability()
    {
        return durability;
    }

    public float getDurabilityNormalized()
    {
        return durability / getContentComponent().getDurability();
    }

    public float getParameter(String name)
    {
        return getContentComponent().getValue(name, getDurability());
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

    @Override
    public void write(Json json)
    {
        json.writeValue("du", durability);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        durability = jsonData.getFloat("du", 0);
    }

    public float decreaseDurability(float value)
    {
        this.durability = Math.max(this.durability - value, 0);
        return this.durability;
    }

    public void increaseDurability(float value)
    {
        this.durability = Math.min(this.durability + value,
            getContentComponent().getDurability());
    }

    public void setDurability(float durability)
    {
        this.durability = durability;
    }
}
