package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.RandomWeightComponent")
public class RandomWeightComponent extends ContentComponent
{
    private int weight;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    public static int Get(Content content)
    {
        RandomWeightComponent weightComponent = content.getComponent(RandomWeightComponent.class);

        if (weightComponent == null)
        {
            if (content instanceof Instrument)
            {
                InstrumentSlotItem slot = ((Instrument) content).getSlotItem();

                if (slot == null)
                    return 0;

                return RandomWeightComponent.Get(slot);
            }

            return 0;
        }

        return weightComponent.getWeight();
    }

    public static <T extends Content> T GetRandomItem(Array<T> items)
    {
        return GetRandomItem(items, MathUtils.random(0xFFFFFFFFL));
    }

    public static <T extends Content> T GetRandomItem(Array<T> items, long r)
    {
        int totalWeight = 0;
        for (T i : items)
        {
            totalWeight += RandomWeightComponent.Get(i);
        }

        if (totalWeight == 0)
        {
            if (items.size > 0)
            {
                return items.get(0);
            }

            return null;
        }

        long random = r % totalWeight;

        for (int i = 0; i < items.size; ++i)
        {
            random -= RandomWeightComponent.Get(items.get(i));
            if (random <= 0.0d)
            {
                return items.get(i);
            }
        }

        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.weight = jsonData.getInt("weight");
    }

    public int getWeight()
    {
        return weight;
    }
}
