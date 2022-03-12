package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ClientWeaponComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.WeaponData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientWeaponComponent")
public class ClientWeaponComponent extends ClientInstrumentComponent
{
    private float blowBack;

    @Override
    public ClientWeaponComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientWeaponComponentData((WeaponData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        blowBack = jsonData.getFloat("blowBack", 0.2f);
    }

    public float getBlowBack()
    {
        return blowBack;
    }

}
