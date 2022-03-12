package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.SimplePhysicsComponent")
public class SimplePhysicsComponent extends ContentComponent
{
    private Vector2 size;
    private float mass;
    private float friction;
    private float reduce;
    private boolean fixable;
    private boolean sticky;
    private float contactGap;
    private float fixtureSpeed;
    private boolean rotateBySpeed;
    private float speedLimit;

    public SimplePhysicsComponent()
    {
        size = new Vector2();
        friction = 0;
        contactGap = 1;
        reduce = Constants.Core.PHY_COLLISION_REDUCING;
        fixable = true;
        sticky = false;
        fixtureSpeed = 1;
        rotateBySpeed = false;
        speedLimit = 60;
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new SimplePhysicsComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        size.set(jsonData.getFloat("width"), jsonData.getFloat("height"));
        mass = jsonData.getFloat("mass");
        reduce = jsonData.getFloat("reduce", Constants.Core.PHY_COLLISION_REDUCING);
        fixable = jsonData.getBoolean("fixable", true);
        sticky = jsonData.getBoolean("sticky", false);
        speedLimit = jsonData.getFloat("speedLimit", speedLimit);

        contactGap = jsonData.getFloat("contactGap", contactGap);
        friction = jsonData.getFloat("friction", friction);
        fixtureSpeed = jsonData.getFloat("fixtureSpeed", fixtureSpeed);

        rotateBySpeed = jsonData.getBoolean("rotateBySpeed", rotateBySpeed);
    }

    public float getSpeedLimit()
    {
        return speedLimit;
    }

    public Vector2 getSize()
    {
        return size;
    }

    public float getMass()
    {
        return mass;
    }

    public float getFriction()
    {
        return friction;
    }

    public float getReduce()
    {
        return reduce;
    }

    public boolean isFixable()
    {
        return fixable;
    }

    public boolean isSticky()
    {
        return sticky;
    }

    public float getContactGap()
    {
        return contactGap;
    }

    public float getFixtureSpeed()
    {
        return fixtureSpeed;
    }

    public boolean isRotateBySpeed()
    {
        return rotateBySpeed;
    }
}
