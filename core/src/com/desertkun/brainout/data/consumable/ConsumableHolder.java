package com.desertkun.brainout.data.consumable;

import com.desertkun.brainout.content.consumable.ConsumableItem;

public interface ConsumableHolder
{
    public static class RequireResult
    {
        public int amount;
        public int quality;
        public ConsumableItem item;

        public RequireResult(ConsumableItem item, int amount, int quality)
        {
            this.item = item;
            this.amount = amount;
            this.quality = quality;
        }
    }

    public int put(ConsumableItem item, int amount, int quality, String tag, boolean checkDirection);
    public RequireResult require(int amount, String tag);
}
