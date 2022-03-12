package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.ServerSwampComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerSwampComponent")
public class ServerSwampComponent extends ContentComponent
{
    private String activateEffect;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerSwampComponentData((BlockData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        activateEffect = jsonValue.getString("activateEffect");
    }

    public String getActivateEffect()
    {
        return activateEffect;
    }
}
