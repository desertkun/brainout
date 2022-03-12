package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ServerItemComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.server.ServerConstants;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerItemComponent")
public class ServerItemComponent extends ContentComponent
{
    private float timeToLive;
    private float dropSpeed;
    private String target;
    private ObjectMap<ConsumableContent, Integer> loadWith;

    public ServerItemComponent()
    {
        loadWith = new ObjectMap<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerItemComponentData((ItemData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.timeToLive = jsonData.getFloat("timeToLive", 0);
        this.dropSpeed = jsonData.getFloat("dropSpeed", ServerConstants.Drop.DROP_SPEED_THROW);
        this.target = jsonData.getString("target", "");

        if (jsonData.has("loadWith"))
        {
            for (JsonValue value : jsonData.get("loadWith"))
            {
                ConsumableContent cnt = BrainOutServer.ContentMgr.get(value.name(), ConsumableContent.class);

                loadWith.put(cnt, value.asInt());
            }
        }
    }

    public ObjectMap<ConsumableContent, Integer> getLoadWith()
    {
        return loadWith;
    }

    public String getTarget()
    {
        return target;
    }

    public float getTimeToLive()
    {
        return timeToLive;
    }

    public float getDropSpeed()
    {
        return dropSpeed;
    }
}
