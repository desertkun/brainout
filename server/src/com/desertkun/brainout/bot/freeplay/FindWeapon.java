package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntSet;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskFollowAndShootTarget;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.utils.RandomValue;

public class FindWeapon extends Task
{
    private final IntSet weaponBlackList;

    public FindWeapon(TaskStack stack, IntSet weaponBlackList)
    {
        super(stack);

        this.weaponBlackList = weaponBlackList;
    }

    @Override
    protected void update(float dt)
    {
        // we need to find a weapon

        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        popMeAndPushTask(new FindItem(getStack(), new FindItem.ItemPredicate()
        {
            @Override
            public int matches(ItemData itemData, ConsumableRecord record)
            {
                if (weaponBlackList.contains(itemData.getId()))
                    return 0;

                return matchWeapon(record);
            }

            @Override
            public void notFound()
            {
                pop();
            }
        }, (stack, enemy) ->
        {
            // we have only knife atm so attack only on short distance
            if (Vector2.dst2(enemy.getX(), enemy.getY(), playerData.getX(), playerData.getY()) > 8.0f * 8.0f)
                return false;

            pushTask(new TaskFollowAndShootTarget(getStack(), enemy,
                new RandomValue(0.3f, 0.8f), new RandomValue(0.5f, 1.0f)));

            return true;
        }, (record) ->
        {
            selectInstrument(record);
            reloadWeapon();
        }));
    }

    private int matchWeapon(ConsumableRecord record)
    {
        ConsumableItem item = record.getItem();

        if (item instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

            if (ici.getInstrumentData() instanceof WeaponData)
            {
                WeaponData wp = ((WeaponData) ici.getInstrumentData());
                Slot slot = wp.getWeapon().getSlot();
                if (slot != null && (slot.getID().equals("slot-primary") ||
                        slot.getID().equals("slot-secondary")))
                {
                    return 1;
                }
            }
        }

        return 0;
    }
}
