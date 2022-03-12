package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.data.components.WeaponAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.WeaponData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.WeaponAnimationComponent")
public class WeaponAnimationComponent extends InstrumentAnimationComponent
{
    public WeaponAnimationComponent()
    {
    }

    @Override
    public WeaponAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new WeaponAnimationComponentData((WeaponData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }
}
