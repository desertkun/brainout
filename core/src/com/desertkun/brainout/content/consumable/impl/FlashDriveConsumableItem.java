package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.FlashDrive;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.impl.FlashDriveConsumableItem")
public class FlashDriveConsumableItem extends ConsumableItem
{
    private FlashDrive flashDrive;
    private String code;

    public FlashDriveConsumableItem(FlashDrive flashDrive)
    {
        this.flashDrive = flashDrive;
        this.code = "";
    }

    public FlashDriveConsumableItem()
    {
        this.flashDrive = null;
    }

    @Override
    public ConsumableContent getContent()
    {
        return flashDrive;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return false;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        code = jsonData.getString("code", "");

        if (jsonData.has("content"))
        {
            flashDrive = ((FlashDrive) BrainOut.ContentMgr.get(jsonData.getString("content")));
        }
        else
        {
            flashDrive = null;
        }
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("code", code);

        if (flashDrive != null)
            json.writeValue("content", flashDrive.getID());
    }
}
