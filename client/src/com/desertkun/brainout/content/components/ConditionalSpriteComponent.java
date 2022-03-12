package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ConditionalSpriteComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ConditionalSpriteComponent")
public class ConditionalSpriteComponent extends ContentComponent
{
    private final Array<Condition> conditions;

    public static class Condition
    {
        public String ownable;
        public String sprite;
    }

    private String defaultSprite;

    public ConditionalSpriteComponent()
    {
        conditions = new Array<>();
    }

    @Override
    public ConditionalSpriteComponentData getComponent(ComponentObject componentObject)
    {
        return new ConditionalSpriteComponentData((ActiveData) componentObject, this);
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        for (JsonValue value : jsonData.get("conditions"))
        {
            Condition condition = new Condition();
            condition.sprite = value.getString("sprite");
            condition.ownable = value.getString("ownable");
            conditions.add(condition);
        }

        defaultSprite = jsonData.getString("defaultSprite");
    }

    public String getDefaultSprite()
    {
        return defaultSprite;
    }

    public Array<Condition> getConditions()
    {
        return conditions;
    }
}
