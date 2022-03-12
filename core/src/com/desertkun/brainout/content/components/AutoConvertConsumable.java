package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.AutoConvertConsumable")
public class AutoConvertConsumable extends ContentComponent
{
    private ConsumableContent convertTo;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    public ConsumableContent getConvertTo()
    {
        return convertTo;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        convertTo = BrainOut.ContentMgr.get(jsonData.getString("to"), ConsumableContent.class);
    }
}
