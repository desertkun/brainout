package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.AmbientSoundData;
import com.desertkun.brainout.data.components.AmbientSoundComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.AmbientSoundComponent")
public class AmbientSoundComponent extends ActiveEffectComponent
{
    @Override
    public AmbientSoundComponentData getComponent(ComponentObject componentObject)
    {
        return new AmbientSoundComponentData((AmbientSoundData)componentObject, this) ;
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
    }
}
