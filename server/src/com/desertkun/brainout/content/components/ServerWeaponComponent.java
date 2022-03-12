package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.WeaponData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerWeaponComponent")
public class ServerWeaponComponent extends ContentComponent
{
    private ObjectMap<String, String> effects;
    private boolean usedEvent;

    public ServerWeaponComponent()
    {
        effects = new ObjectMap<>();
    }

    @Override
    public ServerWeaponComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerWeaponComponentData((WeaponData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {
        //
    }

    public boolean isUsedEvent()
    {
        return usedEvent;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        usedEvent = jsonData.getBoolean("usedEvent", false);

        if (jsonData.has("effects"))
        {
            for (JsonValue entry : jsonData.get("effects"))
            {
                effects.put(entry.name(), entry.asString());
            }
        }
    }

    public ObjectMap<String, String> getEffects()
    {
        return effects;
    }
}
