package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.components.WeaponHeatComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.WeaponHeatComponent")
public class WeaponHeatComponent extends ContentComponent
{
    private ParticleEffect effect;
    private float max;
    private float attenuation;
    private float launchAdd;
    private String attachBone;

    @Override
    public WeaponHeatComponentData getComponent(ComponentObject componentObject)
    {
        return new WeaponHeatComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effect = BrainOutClient.ContentMgr.get(jsonData.getString("effect"), ParticleEffect.class);
        max = jsonData.getFloat("max", 100);
        attenuation = jsonData.getFloat("attenuation", 20);
        launchAdd = jsonData.getFloat("launch-add", 1);
        attachBone = jsonData.getString("attach-bone");
    }

    public ParticleEffect getEffect()
    {
        return effect;
    }

    public float getMax()
    {
        return max;
    }

    public float getAttenuation()
    {
        return attenuation;
    }

    public float getLaunchAdd()
    {
        return launchAdd;
    }

    public String getAttachBone()
    {
        return attachBone;
    }
}
