package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ItemComponent")
public class ItemComponent extends ContentComponent
{
    private Item dropItem;
    private float weight;
    private Array<String> tags;
    private boolean space;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    public boolean isSpace()
    {
        return space;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("weight"))
        {
            this.weight = jsonData.getFloat("weight");
        }
        else
        {
            this.weight = 0;
        }

        space = jsonData.getBoolean("space", false);

        JsonValue tt = jsonData.get("consumableTags");
        if (tt != null)
        {
            tags = new Array<>();
            if (tt.isString())
            {
                tags.add(tt.asString());
            }
            else
            {
                for (JsonValue value : tt)
                {
                    tags.add(value.asString());
                }
            }
        }

        if (jsonData.has("dropItem") && jsonData.get("dropItem").isNull())
        {
            this.dropItem = null;
        }
        else
        {
            this.dropItem = (Item) BrainOut.ContentMgr.get(jsonData.getString("dropItem",
                    Constants.Drop.DEFAULT_DROP_ITEM));
        }
    }

    public Array<String> getTags(Content content)
    {
        if (content instanceof Weapon && tags == null)
        {
            tags = new Array<>();
            tags.add("weapon");

            Weapon weapon = ((Weapon) content);
            if (weapon.getSlotItem() != null && weapon.getSlotItem().getCategory() != null)
            {
                tags.add(weapon.getSlotItem().getCategory());
            }

            return tags;
        }

        return tags;
    }

    public float getWeight()
    {
        return weight;
    }

    public Item getDropItem()
    {
        return dropItem;
    }
}
