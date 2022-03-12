package com.desertkun.brainout.data.active;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.FreeplayGenerator;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.FreeplayGeneratorData")
public class FreeplayGeneratorData extends PointData
{
    private float fuel;
    private float working;
    private int failCounter;

    @InspectableProperty(name="RequiredItem", kind= PropertyKind.string, value= PropertyValue.vString)
    public String requiredItem;

    public FreeplayGeneratorData(FreeplayGenerator generator, String dimension)
    {
        super(generator, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        fuel = jsonData.getFloat("fuel", fuel);
        working = jsonData.getFloat("wkn", working);
        requiredItem = jsonData.getString("rq", requiredItem);

        failCounter = 1;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("fuel", fuel);
        json.writeValue("wkn", working);

        if (requiredItem != null)
        {
            json.writeValue("rq", requiredItem);
        }
    }

    public ItemData findRequiredItem()
    {
        if (requiredItem == null)
            return null;

        Map map = getMap();

        ActiveData activeData = map.getActiveNameIndex().get(requiredItem);

        if (activeData instanceof ItemData)
        {
            return ((ItemData) activeData);
        }

        return null;
    }

    private boolean fail()
    {
        failCounter--;
        if (failCounter >= 0)
        {
            return true;
        }

        failCounter = MathUtils.random(1, 3);
        return false;
    }

    public boolean activate()
    {
        if (isEmpty())
            return false;

        if (isWorking())
            return false;

        FreeplayGenerator g = ((FreeplayGenerator) getContent());

        if (fail())
        {
            working = 0.5f;
        }
        else
        {
            working = Math.min(MathUtils.random(g.getWorkingTimeFrom(), g.getWorkingTimeTo()), fuel);
        }

        updated();

        return true;
    }

    public Instrument getPetrol()
    {
        return getGenerator().getPetrol();
    }

    public float getFuel()
    {
        return fuel;
    }

    public boolean hasFailed()
    {
        return working <= 1.0f;
    }

    public void addFuel()
    {
        this.fuel += getGenerator().getRefillAmount();
        updated();
    }

    public boolean isEmpty()
    {
        return fuel <= 0;
    }

    public boolean isRequiredItemFulfilled()
    {
        ItemData itemData = findRequiredItem();

        if (itemData == null)
            return true;

        Item item = ((Item) itemData.getCreator());

        if (item.getFilters() == null)
            return true;

        return item.fulfilled(itemData.getRecords());
    }

    public boolean isWorking()
    {
        return working > 0;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (isEmpty())
        {
            if (isWorking())
            {
                stop();
            }
        }
        else
        {
            if (isWorking())
            {
                working -= dt;
                fuel -= dt;
            }
        }
    }

    private void stop()
    {
        working = 0;
        updated();
    }

    public FreeplayGenerator getGenerator()
    {
        return ((FreeplayGenerator) getContent());
    }

    public float getWorkingTime()
    {
        return working;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
