package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.components.AmmoResourceDispenserComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.AmmoResourceDispenserComponent")
public class AmmoResourceDispenserComponent extends ResourceDispenserComponent
{
    private ArrayMap<ConsumableContent, DispenserLimits> consumables;

    public static class DispenserLimits implements Json.Serializable
    {
        public int amount;
        public int deliverAtTime;
        public int minToHave;

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            amount = jsonData.getInt("amount");
            deliverAtTime = jsonData.getInt("deliverAtTime");
            minToHave = jsonData.getInt("minToHave");
        }
    }

    public AmmoResourceDispenserComponent()
    {
        consumables = new ArrayMap<ConsumableContent, DispenserLimits>();
    }

    @Override
    public AmmoResourceDispenserComponentData getComponent(ComponentObject componentObject)
    {
        return new AmmoResourceDispenserComponentData(componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("consumables"))
        {
            JsonValue cosumablesV = jsonData.get("consumables");

            if (cosumablesV.isObject())
            {
                for (JsonValue c: cosumablesV)
                {
                    ConsumableContent cc = ((ConsumableContent) BrainOut.ContentMgr.get(c.name()));

                    DispenserLimits limits = new DispenserLimits();
                    limits.read(json, c);

                    consumables.put(cc, limits);
                }
            }
        }
    }

    public ArrayMap<ConsumableContent, DispenserLimits> getConsumables()
    {
        return consumables;
    }
}
