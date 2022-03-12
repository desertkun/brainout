package com.desertkun.brainout.bot.freeplay;

import com.desertkun.brainout.bot.EnemyNoticedCallback;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ApproachItem extends TakeItem
{
    public ApproachItem(TaskStack stack, ItemData item, ConsumableRecord record, int takeAmount, EnemyNoticedCallback enemyNoticedCallback)
    {
        super(stack, item, record, takeAmount, enemyNoticedCallback, null);
    }

    @Override
    protected void takeOutTheItem()
    {
        pop();
    }
}
