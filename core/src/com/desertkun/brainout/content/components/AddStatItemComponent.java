package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.AddStatItemComponent")
public class AddStatItemComponent extends StoreItemComponent
{
    private String stat;
    private int amount;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.stat = jsonData.getString("stat");
        this.amount = jsonData.getInt("amount", 1);
    }

    public String getStat()
    {
        return stat;
    }

    public int getAmount()
    {
        return amount;
    }
}
