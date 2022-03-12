package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.UnlockStoreItemComponent")
public class UnlockStoreItemComponent extends StoreItemComponent
{
    private OwnableContent content;
    private int amount;
    private boolean unique;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public OwnableContent getContent()
    {
        return content;
    }

    public int getAmount()
    {
        return amount;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.content = BrainOut.ContentMgr.get(jsonData.getString("content"), OwnableContent.class);
        this.amount = jsonData.getInt("amount", 1);
        this.unique = jsonData.getBoolean("unique", false);
    }

    public boolean isUnique()
    {
        return unique;
    }
}
