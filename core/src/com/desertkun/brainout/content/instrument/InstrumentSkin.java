package com.desertkun.brainout.content.instrument;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.InstrumentSkin")
public class InstrumentSkin extends Skin
{
    private InstrumentSlotItem slotItem;
    private boolean preferIcon;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        preferIcon = jsonData.getBoolean("preferIcon", false);
        if (jsonData.has("slot"))
        {
            this.slotItem = ((InstrumentSlotItem) BrainOut.ContentMgr.get(jsonData.getString("slot")));
        }
    }

    public boolean isPreferIcon()
    {
        return preferIcon;
    }

    public InstrumentSlotItem getSlotItem()
    {
        return slotItem;
    }
}
