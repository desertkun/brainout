package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.components.interfaces.UpgradeComponent;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ProvideMagazineComponent")
public class ProvideMagazineComponent extends ContentComponent implements UpgradeComponent
{
    public ProvideMagazineComponent()
    {
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Json json, JsonValue jsonData)
    {
    }

    @Override
    public void upgrade(InstrumentData instrumentData)
    {
        if (!(instrumentData instanceof WeaponData))
            return;
        WeaponData weaponData = ((WeaponData) instrumentData);
        ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);
        if (sw == null)
            return;
        ServerWeaponComponentData.Slot slot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
        slot.generateNecessaryMagazines();
    }

    @Override
    public boolean pre()
    {
        return false;
    }
}
