package com.desertkun.brainout.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.data.components.WeaponDurabilityComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.WeaponDurabilityComponent")
public class WeaponDurabilityComponent extends DurabilityComponent
{
    private float wear;

    @Override
    public WeaponDurabilityComponentData getComponent(ComponentObject componentObject)
    {
        return new WeaponDurabilityComponentData(componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.wear = jsonData.getFloat("wear", 1000);
    }

    public float getWear()
    {
        return wear;
    }
}
