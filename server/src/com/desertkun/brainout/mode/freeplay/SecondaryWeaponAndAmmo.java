package com.desertkun.brainout.mode.freeplay;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.esotericsoftware.minlog.Log;

public class SecondaryWeaponAndAmmo
{
    private static Array<String> tmp2 = new Array<>();

    public static void generate(ServerFreeRealization free, ConsumableContainer cnt, String dimension)
    {
        generate(free, cnt, dimension, null);
    }

    public static void generate(ServerFreeRealization free, ConsumableContainer cnt, String dimension, ActiveData owner)
    {
        // a gun and some ammo into it
        InstrumentSlotItem slot = free.getRandomSecondaryWeapon();

        if (slot == null)
            return;

        Weapon weapon = ((Weapon) slot.getInstrument());
        if (Log.INFO) Log.info("Generated new weapon " + weapon.getID());
        WeaponData weaponData = weapon.getData(dimension);
        if (owner != null)
        {
            weaponData.setOwner(owner);
        }

        // skin
        {
            weaponData.setSkin(weapon.getDefaultSkin());
        }

        /*
        // setup random visual upgrades
        {
            int numUpgrades = MathUtils.random(0, 5);

            tmp2.clear();
            for (ObjectMap.Entry<String, Array<Upgrade>> entry : slot.getUpgrades())
            {
                Array<Upgrade> ups = entry.value;

                if (ups.size == 0)
                    continue;

                if (ups.get(0) instanceof UpgradeChain.ChainedUpgrade)
                    continue;

                tmp2.add(entry.key);
            }
            tmp2.shuffle();

            for (int i = 0; i < numUpgrades && i < tmp2.size; i++)
            {
                String key = tmp2.get(i);
                Upgrade u = slot.getUpgrades().get(key).random();

                weaponData.getUpgrades().put(key, u);
            }
        }

        // setup random non-visual upgrades
        {
            int numUpgrades = MathUtils.random(0, 5);

            tmp2.clear();
            for (ObjectMap.Entry<String, Array<Upgrade>> entry : slot.getUpgrades())
            {
                Array<Upgrade> ups = entry.value;

                if (ups.size == 0)
                    continue;

                if (!(ups.get(0) instanceof UpgradeChain.ChainedUpgrade))
                    continue;

                tmp2.add(entry.key);
            }
            tmp2.shuffle();

            for (int i = 0; i < numUpgrades && i < tmp2.size; i++)
            {
                String key = tmp2.get(i);
                Upgrade u = slot.getUpgrades().get(key).random();

                weaponData.getUpgrades().put(key, u);
            }
        }
        */

        // ammo
        {
            ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);
            if (sw != null)
            {
                Bullet bullet = BrainOutServer.ContentMgr.get(weaponData.getWeapon().getPrimaryProperties().getBullet(), Bullet.class);

                if (bullet != null)
                {
                    switch (MathUtils.random(4))
                    {
                        case 0:
                        case 1:
                        case 2:
                        {
                            // full mag
                            cnt.putConsumable(weapon.getPrimaryProperties().getClipSize(),
                                    new DefaultConsumableItem(bullet));
                            break;
                        }
                        case 3:
                        case 4:
                        {
                            // two mags
                            cnt.putConsumable(weapon.getPrimaryProperties().getClipSize() * 2,
                                    new DefaultConsumableItem(bullet));
                            break;
                        }
                    }
                }
            }
        }

        cnt.putConsumable(1, new InstrumentConsumableItem(weaponData, dimension));
    }
}
