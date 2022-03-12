package com.desertkun.brainout.mode.freeplay;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.content.upgrades.UpgradeChain;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.esotericsoftware.minlog.Log;

public class RandomAmmo
{
    public static void generate(ServerFreeRealization free, ConsumableContainer cnt)
    {
        // get random bullet
        Bullet bullet = free.getRandomBullet();

        if (bullet == null)
            return;

        if (Log.INFO) Log.info("Generated ammo " + bullet.getID());

        switch (MathUtils.random(7))
        {
            case 0:
            case 1:
            case 2:
            case 3:
            {
                // full mag
                cnt.putConsumable(bullet.getGood(),
                        new DefaultConsumableItem(bullet));
                break;
            }
            case 4:
            case 5:
            {
                // half of mag
                cnt.putConsumable(bullet.getGood() / 2,
                        new DefaultConsumableItem(bullet));
                break;
            }
            case 6:
            {
                // two mags
                cnt.putConsumable(bullet.getGood() * 2,
                        new DefaultConsumableItem(bullet));
                break;
            }
        }
    }
}
