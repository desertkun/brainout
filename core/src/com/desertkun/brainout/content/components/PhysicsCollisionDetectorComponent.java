package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.PhysicsCollisionDetectorComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PhysicsCollisionDetectorComponent")
public class PhysicsCollisionDetectorComponent extends ContentComponent
{
    private float distance;

    public PhysicsCollisionDetectorComponent()
    {
        this.distance = 0.5f;
    }

    @Override
    public PhysicsCollisionDetectorComponentData getComponent(ComponentObject componentObject)
    {
        return new PhysicsCollisionDetectorComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        distance = jsonData.getFloat("distance", distance);
    }

    public float getDistance()
    {
        return distance;
    }
}
