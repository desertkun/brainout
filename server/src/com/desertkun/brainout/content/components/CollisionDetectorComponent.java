package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.CollisionDetectorComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.CollisionDetectorComponent")
public class CollisionDetectorComponent extends ContentComponent
{
    private String detectClass;

    @Override
    public CollisionDetectorComponentData getComponent(ComponentObject componentObject)
    {
        return new CollisionDetectorComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.detectClass = jsonData.getString("detectClass");
    }

    public String getDetectClass()
    {
        return detectClass;
    }
}
