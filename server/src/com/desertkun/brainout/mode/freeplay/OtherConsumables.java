package com.desertkun.brainout.mode.freeplay;

import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.esotericsoftware.minlog.Log;

public class OtherConsumables
{
    public static void generate(ServerFreeRealization free, ConsumableContainer cnt)
    {
        ConsumableContent consumable = free.getRandomConsumable();

        if (consumable == null)
            return;

        if (Log.INFO) Log.info("Generated new consumable " + consumable.getID());
        cnt.putConsumable(1, consumable.acquireConsumableItem());
    }

    public static void generateJunk(ServerFreeRealization free, ConsumableContainer cnt)
    {
        ConsumableContent consumable = free.getRandomJunk();

        if (consumable == null)
            return;

        if (Log.INFO) Log.info("Generated new junk " + consumable.getID());
        cnt.putConsumable(1, consumable.acquireConsumableItem());
    }
}
