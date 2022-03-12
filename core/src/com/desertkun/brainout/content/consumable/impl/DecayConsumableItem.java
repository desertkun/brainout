package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.DecayConsumableContent;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("decayconsit")
@ReflectAlias("content.consumable.impl.DecayConsumableItem")
public class DecayConsumableItem extends DefaultConsumableItem
{
    private int max;
    private int uses;

    public DecayConsumableItem(DecayConsumableContent cnt)
    {
        super(cnt);

        this.max = cnt.getUses();
        this.uses = cnt.getUses();
    }

    public DecayConsumableItem()
    {
        super();
    }

    public int getUses()
    {
        return uses;
    }

    public int getMax()
    {
        return max;
    }

    public void setUses(int uses)
    {
        this.uses = uses;
    }

    public int getUsesLeftPercent()
    {
        return (int)((((float)uses / (float)max)) * 100.f);
    }

    public boolean use(ConsumableContainer container, ConsumableRecord record)
    {
        uses--;

        if (uses <= 0)
        {
            container.getConsumable(1, record);
            return true;
        }

        return false;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return false;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);
        json.writeValue("uses", uses);
        json.writeValue("max", max);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
        uses = jsonData.getInt("uses", uses);
        max = jsonData.getInt("max", max);
    }
}
