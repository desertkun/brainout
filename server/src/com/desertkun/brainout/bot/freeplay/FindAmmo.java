package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.utils.IntSet;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class FindAmmo extends Task
{
    private final IntSet weaponBlackList;
    private final ConsumableRecord weaponRecord;
    private final Bullet bullet;

    public FindAmmo(TaskStack stack, IntSet weaponBlackList, Bullet bullet, ConsumableRecord weaponRecord)
    {
        super(stack);

        if (weaponBlackList == null)
            this.weaponBlackList = new IntSet();
        else
            this.weaponBlackList = weaponBlackList;

        this.weaponRecord = weaponRecord;
        this.bullet = bullet;
    }

    @Override
    protected void update(float dt)
    {
        // we need to find a weapon

        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        // we need to find ammo
        popMeAndPushTask(new FindItem(getStack(), new FindItem.ItemPredicate()
        {
            @Override
            public int matches(ItemData itemData, ConsumableRecord record)
            {
                ConsumableItem item = record.getItem();

                if (item.getContent() == bullet)
                {
                    return record.getAmount();
                }

                return 0;
            }

            @Override
            public void notFound()
            {
                dropWeapon(weaponRecord);
            }
        }, (stack, enemy) -> false, (record) ->
        {
            selectInstrument(weaponRecord);
            reloadWeapon();
            pop();
        }));
    }

    private void dropWeapon(ConsumableRecord weaponRecord)
    {
        ItemData itemData = getController().dropConsumable(weaponRecord.getId(), getPlayerData().getAngle(), 1);

        if (itemData != null)
        {
            weaponBlackList.add(itemData.getId());
        }
    }
}
