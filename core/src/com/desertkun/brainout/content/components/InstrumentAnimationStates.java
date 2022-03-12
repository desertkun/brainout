package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentAnimationStates")
public class InstrumentAnimationStates implements Json.Serializable
{
    private ObjectMap<String, String> animations;

    public enum State
    {
        hold,
        aim,
        reload,
        reloadSecondary,
        reloadBoth,
        hit,
        aimHit,
        pull,
        pullSecondary,
        cock,
        cockSecondary,
        custom,
        reset,
        buildUp,
        fetch,
        fetchSecondary,
        addRound
    }

    public InstrumentAnimationStates()
    {
        try
        {
            animations = new ObjectMap<>(8);
        }
        catch (IllegalArgumentException fuckOffYouFilthyThreading)
        {
            animations = new ObjectMap<>(2);
        }
    }

    public InstrumentAnimationStates(InstrumentAnimationStates states)
    {
        this();

        update(states);
    }

    public void update(InstrumentAnimationStates states)
    {
        animations.putAll(states.animations);
    }

    public String getAnimation(String state)
    {
        return animations.get(state);
    }

    public String getAnimation(State state)
    {
        return getAnimation(state.toString());
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (!jsonData.isObject()) throw new IllegalArgumentException("Expected to have object, " + jsonData.type() + " instead");

        for (JsonValue value: jsonData)
        {
            animations.put(value.name(), value.asString());
        }
    }
}
