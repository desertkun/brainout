package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.components.PlayerAnimationComponent")
public class PlayerAnimationComponent extends AnimationComponent
{
    private RandomValue stayAnimationTimeScale;

    public PlayerAnimationComponent()
    {
        stayAnimationTimeScale = new RandomValue(1, 1);
    }

    @Override
    public PlayerAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new PlayerAnimationComponentData((PlayerData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("stayTimeScale"))
        {
            stayAnimationTimeScale.read(json, jsonData.get("stayTimeScale"));
        }
    }

    public RandomValue getStayAnimationTimeScale()
    {
        return stayAnimationTimeScale;
    }
}
