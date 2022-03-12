package com.desertkun.brainout.mode.freeplay;

import com.desertkun.brainout.content.consumable.ConsumableToOwnableContent;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.mode.ServerFreeRealization;

public class Containers
{
    public static void generate(ServerFreeRealization free, ConsumableContainer cnt)
    {
        ConsumableToOwnableContent consumable = free.getRandomContainer();

        if (consumable == null)
            return;

        cnt.putConsumable(1, consumable.acquireConsumableItem());
    }
}
