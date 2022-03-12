package com.desertkun.brainout.plugins;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

public class Plugin implements Json.Serializable
{
    private boolean enabled;

    public Plugin()
    {

    }

    public void init()
    {

    }

    public void release()
    {

    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.enabled = jsonData.getBoolean("enabled", true);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void reset()
    {
        //
    }
}
