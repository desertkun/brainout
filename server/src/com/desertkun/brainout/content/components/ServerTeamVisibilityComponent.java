package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerTeamVisibilityComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerTeamVisibilityComponent")
public class ServerTeamVisibilityComponent extends ContentComponent
{
    private float detectionTime;

    @Override
    public ServerTeamVisibilityComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerTeamVisibilityComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        detectionTime = jsonData.getFloat("detectionTime", 5);
    }

    public float getDetectionTime()
    {
        return detectionTime;
    }

    public void setDetectionTime(float detectionTime)
    {
        this.detectionTime = detectionTime;
    }
}
