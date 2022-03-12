package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveEffectComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveEffectComponent")
public class ActiveEffectComponent extends ContentComponent
{
    private Effect effect;
    private Vector2 offset;

    public ActiveEffectComponent()
    {
        this.offset = new Vector2();
    }

    @Override
    public ActiveEffectComponentData getComponent(ComponentObject componentObject)
    {
        return new ActiveEffectComponentData((ActiveData)componentObject, this);
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
        effect = ((Effect) BrainOutClient.ContentMgr.get(jsonData.getString("effect")));
        this.offset.set(jsonData.getFloat("offsetX", 0), jsonData.getFloat("offsetY", 0));
    }

    public Effect getEffect()
    {
        return effect;
    }

    public void setEffect(Effect effect)
    {
        this.effect = effect;
    }
}
