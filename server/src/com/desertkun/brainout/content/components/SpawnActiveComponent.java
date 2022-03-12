package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ExplosiveComponentData;
import com.desertkun.brainout.data.components.SpawnActiveComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.SpawnActiveComponent")
public class SpawnActiveComponent extends ContentComponent
{
    private Active active;
    private String effect;
    private float speedCoef;

    @Override
    public SpawnActiveComponentData getComponent(ComponentObject componentObject)
    {
        return new SpawnActiveComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.active = ((Active) BrainOut.ContentMgr.get(jsonData.getString("active")));
        this.effect = jsonData.getString("effect", "");
        this.speedCoef = jsonData.getFloat("speedCoef", 1.0f);
    }

    public Active getActive()
    {
        return active;
    }

    public String getEffect()
    {
        return effect;
    }

    public float getSpeedCoef()
    {
        return speedCoef;
    }
}
