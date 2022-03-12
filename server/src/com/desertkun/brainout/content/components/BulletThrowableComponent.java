package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BulletThrowableComponent")
public class BulletThrowableComponent extends ContentComponent
{
    private float throwPower;
    private ThrowableActive throwActive;

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
        this.throwPower = jsonData.getFloat("throw-power");
        this.throwActive = ((ThrowableActive) BrainOut.ContentMgr.get(jsonData.getString("throw-active")));
    }

    public float getThrowPower()
    {
        return throwPower;
    }

    public ThrowableActive getThrowActive()
    {
        return throwActive;
    }
}
