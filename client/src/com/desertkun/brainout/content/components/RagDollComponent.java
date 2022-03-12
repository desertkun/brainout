package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.AnimationComponentData;
import com.desertkun.brainout.data.components.RagDollComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.RagDollComponent")
public class RagDollComponent extends ContentComponent
{
    private Class<? extends AnimationComponentData> animationClass;
    private float timeToLive;
    private float detachProbability;
    private Array<String> detachBones;

    public RagDollComponent()
    {
        detachBones = new Array<>();
        detachProbability = 0f;
    }

    @Override
    public RagDollComponentData getComponent(ComponentObject componentObject)
    {
        return new RagDollComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        this.animationClass = (Class<? extends AnimationComponentData>)
                BrainOut.R.forName(jsonData.getString("animationClass"));

        this.timeToLive = jsonData.getFloat("timeToLive");
        this.detachProbability = jsonData.getFloat("detachProbability", 0);

        if (jsonData.has("detachBones"))
        {
            for (JsonValue value : jsonData.get("detachBones"))
            {
                detachBones.add(value.asString());
            }
        }
    }

    public Class<? extends AnimationComponentData> getAnimationClass()
    {
        return animationClass;
    }

    public float getTimeToLive()
    {
        return timeToLive;
    }

    public Array<String> getDetachBones()
    {
        return detachBones;
    }

    public float getDetachProbability()
    {
        return detachProbability;
    }
}
