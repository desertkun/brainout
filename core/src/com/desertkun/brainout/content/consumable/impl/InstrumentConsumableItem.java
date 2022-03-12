package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.Shop;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ici")
@ReflectAlias("content.consumable.impl.InstrumentConsumableItem")
public class InstrumentConsumableItem extends ConsumableItem
{
    private String dimension;
    private InstrumentData instrumentData;

    public InstrumentConsumableItem(InstrumentData instrumentData, String dimension)
    {
        this.dimension = dimension;
        this.instrumentData = instrumentData;
    }

    public InstrumentConsumableItem()
    {
        this.dimension = null;
        this.instrumentData = null;
    }

    @Override
    public boolean hasAutoQuality()
    {
        return false;
    }

    @Override
    public int pickAutoQuality()
    {
        if (MathUtils.random(4) == 0)
        {
            return 100;
        }

        return MathUtils.random(40, 90);
    }

    public static int SortRecords(ConsumableRecord record)
    {
        ConsumableItem item = record.getItem();

        if (item instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
            Instrument instrument = ((Instrument) ici.getContent());
            Slot slot = instrument.getSlot();

            if (slot != null)
            {
                int index = Constants.Inventory.SLOTS.indexOf(slot.getID(), false);

                if (index < 0)
                {
                    return 5000;
                }

                return index * 100;
            }
        }

        return 5000;
    }

    @Override
    public ConsumableContent getContent()
    {
        if (instrumentData == null)
            return null;

        return ((ConsumableContent) instrumentData.getContent());
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return false;
    }

    public InstrumentData getInstrumentData()
    {
        return instrumentData;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("d", dimension);
        json.writeObjectStart("data");
        instrumentData.write(json, Data.ComponentWriter.TRUE, -1);
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.dimension = jsonData.getString("d", "default");

        Map map = Map.Get(this.dimension);

        if (map == null)
            return;

        if (instrumentData == null)
        {
            instrumentData = map.newInstrument(json, jsonData.get("data"));
        }
        else
        {
            instrumentData.read(json, jsonData.get("data"));
        }
    }

    @Override
    public void init()
    {
        instrumentData.init();
    }

    @Override
    public void release()
    {
        instrumentData.release();
    }

    @Override
    public void setOwner(ActiveData activeData)
    {
        instrumentData.setOwner(activeData);
    }
}
