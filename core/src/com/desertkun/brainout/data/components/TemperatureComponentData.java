package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.TemperatureComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("tempc")
@ReflectAlias("data.components.TemperatureComponentData")
public class TemperatureComponentData extends Component<TemperatureComponent> implements Json.Serializable
{
    private float freezing;

    public TemperatureComponentData(ComponentObject componentObject, TemperatureComponent contentComponent)
    {
        super(componentObject, contentComponent);

        freezing = 0;
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

    public void setFreezing(float f)
    {
        freezing = f;
    }

    public float getFreezing()
    {
        return freezing;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("fr", freezing);
    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        freezing = jsonValue.getFloat("fr", freezing);
    }
}
