package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.BulletSpawnItemComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BulletSpawnItemComponent")
public class BulletSpawnItemComponent extends ContentComponent
{
    private Item item;

    @Override
    public BulletSpawnItemComponentData getComponent(ComponentObject componentObject)
    {
        return new BulletSpawnItemComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public Item getItem()
    {
        return item;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        item = BrainOutServer.ContentMgr.get(jsonData.getString("item"), Item.class);
    }
}
