package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.instrument.Chip;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.InstrumentSkin;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ServerChipSpawnerComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerChipSpawnerComponent")
public class ServerChipSpawnerComponent extends ContentComponent
{
    private Item dropItem;
    private Chip chip;

    @Override
    public ServerChipSpawnerComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerChipSpawnerComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        dropItem = (Item)BrainOut.ContentMgr.get(jsonData.getString("dropItem"));
        chip = (Chip)BrainOut.ContentMgr.get(jsonData.getString("chip"));
    }

    public Item getDropItem()
    {
        return dropItem;
    }

    public Chip getChip()
    {
        return chip;
    }
}
