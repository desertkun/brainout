package com.desertkun.brainout.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ShowPurchaseProgressComponent")
public class ShowPurchaseProgressComponent extends ContentComponent
{
    private Slot slot;

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
    public void read(Json json, JsonValue jsonData)
    {
        slot = BrainOut.ContentMgr.get(jsonData.getString("slot"), Slot.class);
    }

    public boolean show(Slot slot)
    {
        return slot == this.slot;
    }
}
