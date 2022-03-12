package com.desertkun.brainout.content.consumable;

import com.desertkun.brainout.content.consumable.impl.FlashDriveConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.FlashDrive")
public class FlashDrive extends ConsumableContent
{
    @Override
    public ConsumableItem acquireConsumableItem()
    {
        return new FlashDriveConsumableItem(this);
    }
}
