package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ClientItemAutoPickerComponentData;
import com.desertkun.brainout.data.components.ClientItemComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientItemAutoPickerComponent")
public class ClientItemAutoPickerComponent extends ContentComponent
{
    private EffectSet effect;
    private JsonValue effectValue;
    private float pickTime;

    public ClientItemAutoPickerComponent()
    {
    }

    @Override
    public ClientItemAutoPickerComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientItemAutoPickerComponentData((ItemData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectValue = jsonData.get("effect");
        pickTime = jsonData.getFloat("pickTime", 0);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (effectValue != null)
        {
            effect = new EffectSet();
            effect.read(effectValue);
            effectValue = null;
        }
    }

    public EffectSet getEffect()
    {
        return effect;
    }

    public float getPickTime()
    {
        return pickTime;
    }
}
