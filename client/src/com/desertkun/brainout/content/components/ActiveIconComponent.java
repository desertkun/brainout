package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveIconComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveIconComponent")
public class ActiveIconComponent extends AnimationComponent
{
    private float rotateByX;
    private float offsetY;

    @Override
    public ActiveIconComponentData getComponent(ComponentObject componentObject)
    {
        return new ActiveIconComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.rotateByX = jsonData.getFloat("rotateByX", 0);
        this.offsetY = jsonData.getFloat("offsetY", 0);
    }

    public float getRotateByX()
    {
        return rotateByX;
    }

    public float getOffsetY()
    {
        return offsetY;
    }
}
