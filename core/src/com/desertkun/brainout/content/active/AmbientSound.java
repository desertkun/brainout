package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.AmbientSoundData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.AmbientSound")
public class AmbientSound extends Active
{
    private String soundEffect;

    @Override
    public AmbientSoundData getData(String dimension)
    {
        return new AmbientSoundData(this, dimension);
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        soundEffect = jsonData.getString("sound");
    }

    public String getSoundEffect()
    {
        return soundEffect;
    }
}
