package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ItemDependencyComponent")
public class ItemDependencyComponent extends ContentComponent
{
    private ObjectSet<OwnableContent> items;

    public ItemDependencyComponent()
    {
        items = new ObjectSet<>();
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

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        for (JsonValue value : jsonValue.get("items"))
        {
            items.add(BrainOutServer.ContentMgr.get(value.asString(), OwnableContent.class));
        }
    }

    public ObjectSet<OwnableContent> getItems()
    {
        return items;
    }

    public boolean satisfied(UserProfile userProfile)
    {
        for (OwnableContent item : getItems())
        {
            if (!item.hasItem(userProfile))
            {
                return false;
            }
        }

        return true;
    }
}
