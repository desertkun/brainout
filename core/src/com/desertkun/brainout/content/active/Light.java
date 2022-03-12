package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.LightEntity;
import com.desertkun.brainout.data.active.LightData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Light")
public class Light extends Active
{
    private LightEntity lightEntity;

    public Light()
    {
        lightEntity = new LightEntity(true);
    }

    @Override
    public LightData getData(String dimension)
    {
        return new LightData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        lightEntity.read(json, jsonData);
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }

    public LightEntity getLightEntity()
    {
        return lightEntity;
    }
}
