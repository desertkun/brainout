package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.SubPointComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.SubPointComponent")
public class SubPointComponent extends ContentComponent
{
    private float takeTime;
    private Team team;
    private SpawnTarget target;
    private Vector2 offset;

    public SubPointComponent()
    {
        offset = new Vector2();
    }

    @Override
    public SubPointComponentData getComponent(ComponentObject componentObject)
    {
         return new SubPointComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public Vector2 getOffset()
    {
        return offset;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.takeTime = jsonData.getFloat("takeTime", 1.0f);

        if (jsonData.has("team"))
        {
            this.team = BrainOut.ContentMgr.get(jsonData.getString("team"), Team.class);
        }

        if (jsonData.has("target"))
        {
            this.target = SpawnTarget.valueOf(jsonData.getString("target"));
        }
        else
        {
            this.target = null;
        }

        if (jsonData.has("offset"))
        {
            JsonValue offset_ = jsonData.get("offset");
            offset.set(offset_.getFloat("x"), offset_.getFloat("y"));
        }
    }

    public float getTakeTime()
    {
        return takeTime;
    }

    public Team getTeam()
    {
        return team;
    }

    public SpawnTarget getTarget()
    {
        return target;
    }
}
