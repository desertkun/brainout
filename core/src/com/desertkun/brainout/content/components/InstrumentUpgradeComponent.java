package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.components.InstrumentUpgradeComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentUpgradeComponent")
public class InstrumentUpgradeComponent extends ContentComponent
{
    private Upgrade upgrade;
    private Slot slot;

    public InstrumentUpgradeComponent()
    {
        upgrade = new Upgrade();
        slot = null;
    }

    @Override
    public InstrumentUpgradeComponentData getComponent(ComponentObject componentObject)
    {
        return new InstrumentUpgradeComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("upgrade"))
        {
            upgrade.read(json, jsonData.get("upgrade"));
        }

        if (jsonData.has("slot"))
        {
            slot = ((Slot) BrainOut.ContentMgr.get(jsonData.getString("slot")));
        }
    }

    public Upgrade getUpgrade()
    {
        return upgrade;
    }

    public Slot getSlot()
    {
        return slot;
    }
}
