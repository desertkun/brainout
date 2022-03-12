package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Flag")
public class Flag extends Active
{
    private float takeTime;
    private float spawnRange;
    private String spawnName;

    @Override
    public ActiveData getData(String dimension)
    {
        return new FlagData(this, dimension);
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

        takeTime = jsonData.getFloat("takeTime");
        spawnRange = jsonData.getFloat("spawnRange", 0);
        spawnName = jsonData.getString("spawn-name", null);
    }

    public String getSpawnName()
    {
        return spawnName;
    }

    public float getTakeTime()
    {
        return takeTime;
    }

    public float getSpawnRange()
    {
        return spawnRange;
    }
}
