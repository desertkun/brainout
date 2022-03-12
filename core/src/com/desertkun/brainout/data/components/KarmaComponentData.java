package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.KarmaComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("KarmaComponent")
@ReflectAlias("data.components.KarmaComponentData")
public class KarmaComponentData extends Component<KarmaComponent> implements Json.Serializable
{
    private int karma;

    public KarmaComponentData(ComponentObject componentObject, KarmaComponent contentComponent)
    {
        super(componentObject, contentComponent);

        karma = 0;
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
        json.writeValue("krm", karma);
    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        karma = jsonValue.getInt("krm", karma);
    }

    public int getKarma()
    {
        return karma;
    }

    public void setKarma(int karma)
    {
        this.karma = karma;
    }
}
