package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.ActiveIconComponentData;
import com.desertkun.brainout.data.components.BulletAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BulletAnimationComponent")
public class BulletAnimationComponent extends AnimationComponent
{
    @Override
    public BulletAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new BulletAnimationComponentData((BulletData) componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }
}
