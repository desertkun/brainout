package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ContactDamageComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ValueRange;

@Reflect("content.components.ContactDamageComponent")
public class ContactDamageComponent extends ContentComponent
{
    private ValueRange speed;
    private ValueRange damage;

    @Override
    public ContactDamageComponentData getComponent(ComponentObject componentObject)
    {
        return new ContactDamageComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        speed = json.readValue("speed", ValueRange.class, jsonData);
        damage = json.readValue("damage", ValueRange.class, jsonData);
    }

    public ValueRange getDamage()
    {
        return damage;
    }

    public ValueRange getSpeed()
    {
        return speed;
    }
}
