package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.BlockAheadDetectorComponentData;
import com.desertkun.brainout.data.components.CollisionDetectorComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BlockAheadDetectorComponent")
public class BlockAheadDetectorComponent extends ContentComponent
{
    private String detectClass;
    private float detectDistance;

    @Override
    public BlockAheadDetectorComponentData getComponent(ComponentObject componentObject)
    {
        return new BlockAheadDetectorComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.detectClass = jsonData.getString("detectClass");
        this.detectDistance = jsonData.getFloat("distance");
    }

    public String getDetectClass()
    {
        return detectClass;
    }

    public float getDetectDistance()
    {
        return detectDistance;
    }
}
