package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.FreePlayAddStatExitComponent")
public class FreePlayAddStatExitComponent extends ContentComponent
{
    private String stat;
    private int amount;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    public String getStat()
    {
        return stat;
    }

    public int getAmount()
    {
        return amount;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        stat = jsonData.getString("stat");
        amount = jsonData.getInt("amount");
    }
}
