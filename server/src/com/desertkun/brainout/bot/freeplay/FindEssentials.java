package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskWait;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerCampFireComponentData;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeFree;

public class FindEssentials
{
    public static boolean EnsureEssentials(Task task, IntSet weaponBlackList)
    {
        PlayerData playerData = task.getPlayerData();
        if (playerData == null)
            return false;

        Map map = playerData.getMap();
        if (map == null)
            return false;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return false;

        WeaponData weaponData = HaveAnyWeapon(poc);

        if (weaponData == null)
        {
            task.pushTask(new FindWeapon(task.getStack(), weaponBlackList));
            return true;
        }

        if (playerData.getCurrentInstrument() != weaponData)
        {
            task.selectInstrument(weaponData);
        }

        ConsumableRecord weaponRecord = poc.findRecord(weaponData);

        ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);
        if (sw == null)
        {
            if (weaponRecord != null)
            {
                DropWeapon(task, weaponRecord, weaponBlackList);
            }
            return false;
        }

        ServerWeaponComponentData.Slot primary = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
        if (primary == null)
        {
            if (weaponRecord != null)
            {
                DropWeapon(task, weaponRecord, weaponBlackList);
            }
            return false;
        }

        Bullet bullet = primary.getBullet();

        if (primary.hasMagazineManagement())
        {
            if (primary.isDetached())
            {
                task.pushTask(new LoadMagazine(task.getStack(), weaponBlackList, bullet, weaponRecord));
            }
            else
            {
                // current mag is not filled up

                if (primary.getRounds() < (primary.getClipSize() / 2))
                {
                    if (HaveBullets(poc, weaponData, bullet, false))
                    {
                        task.pushTask(new LoadMagazine(task.getStack(), weaponBlackList, bullet, weaponRecord));
                    }
                    else
                    {
                        task.pushTask(new FindAmmo(task.getStack(), weaponBlackList, bullet, weaponRecord));
                    }

                    return true;
                }
            }
        }
        else
        {
            if (!HaveBullets(poc, weaponData, bullet, true))
            {
                task.pushTask(new FindAmmo(task.getStack(), weaponBlackList, bullet, weaponRecord));
                return true;
            }
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        if (gameMode instanceof GameModeFree)
        {
            if (((GameModeFree) gameMode).isNight())
            {
                if (!NearFire(playerData))
                {
                    task.pushTask(new TaskSeekFire(task.getStack(), weaponBlackList));
                }
            }
        }

        return false;
    }

    private static boolean NearFire(PlayerData playerData)
    {
        ActiveData fire = playerData.getMap().getClosestActiveForTag(16, playerData.getX(), playerData.getY(),
            ActiveData.class, Constants.ActiveTags.CAMP_FIRE, activeData -> true);

        if (fire == null)
            return false;

        ServerCampFireComponentData camp = fire.getComponent(ServerCampFireComponentData.class);

        if (camp != null)
        {
            return camp.getDuration() > 30;
        }

        return true;
    }

    private static void DropWeapon(Task task, ConsumableRecord weaponRecord, IntSet weaponBlackList)
    {
        ItemData itemData = task.getController().dropConsumable(
            weaponRecord.getId(), task.getPlayerData().getAngle(), 1);

        if (itemData != null)
        {
            weaponBlackList.add(itemData.getId());
        }
    }

    public static boolean HaveBullets(PlayerOwnerComponent poc, WeaponData weaponData, Bullet bullet, boolean countLoaded)
    {
        if (countLoaded)
        {
            ServerWeaponComponentData wp = weaponData.getComponent(ServerWeaponComponentData.class);

            if (wp != null)
            {
                ServerWeaponComponentData.Slot primarySlot = wp.getSlot(Constants.Properties.SLOT_PRIMARY);

                if (primarySlot != null)
                {
                    if (primarySlot.getRounds() > 0)
                    {
                        return true;
                    }
                }
            }
        }

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : poc.getConsumableContainer().getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            if (item.getContent() == bullet)
                return true;
        }

        return false;
    }

    private static WeaponData HaveAnyWeapon(PlayerOwnerComponent poc)
    {
        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : poc.getConsumableContainer().getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

                if (ici.getInstrumentData() instanceof WeaponData)
                {
                    WeaponData wp = ((WeaponData) ici.getInstrumentData());

                    Slot slot = wp.getWeapon().getSlot();

                    if (slot != null && (slot.getID().equals("slot-primary") || slot.getID().equals("slot-secondary")))
                    {
                        return wp;
                    }
                }
            }
        }

        return null;
    }
}
