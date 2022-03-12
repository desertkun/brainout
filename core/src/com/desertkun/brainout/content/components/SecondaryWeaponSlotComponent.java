package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.components.SecondaryWeaponSlotComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.SecondaryWeaponSlotComponent")
public class SecondaryWeaponSlotComponent extends ContentComponent
{
    private Weapon.WeaponProperties weaponProperties;
    private String icon;

    public SecondaryWeaponSlotComponent()
    {
        weaponProperties = new Weapon.WeaponProperties();
        icon = null;
    }

    @Override
    public SecondaryWeaponSlotComponentData getComponent(ComponentObject componentObject)
    {
        return new SecondaryWeaponSlotComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        initProperties();
    }

    private void initProperties()
    {
        weaponProperties.initProperties();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        weaponProperties.read(json, jsonData.get("properties"));
        icon = jsonData.getString("icon", null);
    }

    public Weapon.WeaponProperties getWeaponProperties()
    {
        return weaponProperties;
    }

    public String getIcon()
    {
        return icon;
    }
}
