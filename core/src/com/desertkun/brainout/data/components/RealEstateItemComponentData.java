package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.KarmaComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("RealEstateItemComponentData")
@ReflectAlias("data.components.RealEstateItemComponentData")
public class RealEstateItemComponentData extends Component implements Json.Serializable
{
    private String key;

    public RealEstateItemComponentData(ComponentObject componentObject)
    {
        super(componentObject, null);
    }

    public RealEstateItemComponentData(String key)
    {
        super(null, null);

        setKey(key);
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
        json.writeValue("key", key);
    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        key = jsonValue.getString("key", key);
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
}
