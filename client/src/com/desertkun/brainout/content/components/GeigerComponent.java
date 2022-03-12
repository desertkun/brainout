package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.GeigerComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.GeigerComponent")
public class GeigerComponent extends ContentComponent
{
    private EffectSet sounds;
    private JsonValue soundsSetValue;
    private int highestFrequency;
    private float distanceDivider;

    public GeigerComponent()
    {
        sounds = new EffectSet();
        highestFrequency = 600;
        distanceDivider = 10f;
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new GeigerComponentData((ActiveData) componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        soundsSetValue = jsonData.get("sounds");

        highestFrequency = jsonData.getInt("highest-frequency", highestFrequency);
        distanceDivider = jsonData.getFloat("distance-divider", distanceDivider);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (soundsSetValue != null)
        {
            sounds.read(soundsSetValue);
            soundsSetValue = null;
        }
    }

    public EffectSet getSounds()
    {
        return sounds;
    }

    public int getHighestFrequency()
    {
        return highestFrequency;
    }

    public float getDistanceDivider()
    {
        return distanceDivider;
    }
}
