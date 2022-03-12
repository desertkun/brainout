package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.SpriteBlockComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("sbcd") // short to keep traffic low
public class SpriteBlockComponentData extends Component<SpriteBlockComponent>
    implements Json.Serializable
{
    private int spriteId;

    public SpriteBlockComponentData(ComponentObject componentObject,
                                    SpriteBlockComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.spriteId = -1;
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

    public ActiveData getSprite(Map map)
    {
        return map.getActives().get(spriteId);
    }

    public void setSprite(ActiveData sprite)
    {
        this.spriteId = sprite.getId();
    }

    @Override
    public void write(Json json)
    {
        if (spriteId != -1)
            json.writeValue("sp", spriteId);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        spriteId = jsonData.getInt("sp", spriteId);
    }
}
