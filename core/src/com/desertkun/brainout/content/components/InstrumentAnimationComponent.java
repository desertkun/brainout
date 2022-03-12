package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.data.components.InstrumentAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentAnimationComponent")
public class InstrumentAnimationComponent extends AnimationComponent
{
    private InstrumentAnimationStates states;
    private AttachTo attachTo;
    private float iconScale;

    public enum AttachTo
    {
        primary,
        primaryUpper,
        secondary
    }

    public InstrumentAnimationComponent()
    {
        this.states = null;
        this.attachTo = AttachTo.primary;
        this.iconScale = 1.0f;
    }

    @Override
    public InstrumentAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new InstrumentAnimationComponentData((InstrumentData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        states = json.readValue(InstrumentAnimationStates.class, jsonData.get("states"));
        attachTo = AttachTo.valueOf(jsonData.getString("attach-to", AttachTo.primary.toString()));
        this.iconScale = jsonData.getFloat("icon-scale", iconScale);
    }

    public InstrumentAnimationStates getStates()
    {
        return states;
    }

    public AttachTo getAttachTo()
    {
        return attachTo;
    }

    public float getIconScale()
    {
        return iconScale;
    }
}
