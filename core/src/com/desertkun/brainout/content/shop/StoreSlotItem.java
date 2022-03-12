package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.Limits;

@Reflect("content.shop.StoreSlotItem")
public class StoreSlotItem extends SlotItem
{
    private int limit;

    public StoreSlotItem()
    {
        limit = 0;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        limit = jsonData.getInt("limit", limit);
    }

    @Override
    public Selection getSelection()
    {
        return null;
    }

    public int getLimit()
    {
        return limit;
    }
}
