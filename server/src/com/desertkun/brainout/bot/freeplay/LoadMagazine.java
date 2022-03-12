package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.utils.IntSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.common.msg.client.WeaponMagazineActionMsg;
import com.desertkun.brainout.common.msg.server.OtherPlayerInstrumentActionMsg;
import com.desertkun.brainout.common.msg.server.OtherPlayerMagazineActionMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;

public class LoadMagazine extends Task
{
    private final IntSet weaponBlackList;
    private final ConsumableRecord weaponRecord;
    private final Bullet bullet;

    public LoadMagazine(TaskStack stack, IntSet weaponBlackList, Bullet bullet, ConsumableRecord weaponRecord)
    {
        super(stack);

        this.weaponBlackList = weaponBlackList;
        this.weaponRecord = weaponRecord;
        this.bullet = bullet;
    }

    private boolean validate(float dt)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return false;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        ServerPlayerControllerComponentData pc = playerData.getComponent(ServerPlayerControllerComponentData.class);
        if (poc == null || pc == null)
        {
            return false;
        }

        ConsumableItem item = weaponRecord.getItem();
        if (!(item instanceof InstrumentConsumableItem))
        {
            return false;
        }

        InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
        InstrumentData instrumentData = ici.getInstrumentData();

        if (!(instrumentData instanceof WeaponData))
        {
            return false;
        }

        WeaponData weaponData = ((WeaponData) instrumentData);

        if (weaponData.getOwner() != getPlayerData())
        {
            return false;
        }

        ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);
        if (sw == null)
        {
            return false;
        }

        ServerWeaponComponentData.Slot primary = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
        if (primary == null)
        {
            return false;
        }

        if (primary.isDetached())
        {
            if (primary.isLoadingRoundsInMagazine())
            {
                return true;
            }
            else
            {
                return tryAndLoadOneRound(primary, poc, weaponData, pc, true);
            }
        }
        else
        {
            if (primary.getRounds() < primary.getClipSize() / 2)
            {
                if (primary.unload(poc))
                {
                    notifyReload(pc, weaponData, primary);
                    return true;
                }
            }
            else
            {
                // current mag is fine, load others
                return tryAndLoadOneRound(primary, poc, weaponData, pc, false);
            }
        }

        return true;
    }

    private boolean tryAndLoadOneRound(
        ServerWeaponComponentData.Slot primary, PlayerOwnerComponent poc,
        WeaponData weaponData, ServerPlayerControllerComponentData pc,
        boolean switchWhenDone)
    {
        int best = primary.getBestLoadedMagazine(-1);
        if (best >= 0)
        {
            ServerWeaponComponentData.Slot.Magazine loaded = primary.getMagazines().get(best);
            if (loaded != null)
            {
                if (loaded.rounds == 0 && !FindEssentials.HaveBullets(poc, weaponData, bullet, false))
                {
                    popMeAndPushTask(new FindAmmo(getStack(), weaponBlackList, bullet, weaponRecord));
                    return true;
                }

                if (loaded.rounds >= primary.getClipSize() ||
                        !FindEssentials.HaveBullets(poc, weaponData, bullet, false))
                {
                    if (switchWhenDone)
                    {
                        if (primary.attach(best))
                        {
                            notifyReload(pc, weaponData, primary);
                            return true;
                        }
                    }
                    else
                    {
                        // we're done
                        return false;
                    }
                }
                else
                {
                    if (primary.loadMagazineBullet(poc, best))
                    {
                        notifyAddBullet(pc, weaponData, primary);
                    }
                    return true;
                }
            }
        }

        return true;
    }

    private void notifyReload(ServerPlayerControllerComponentData pc, WeaponData weaponData, ServerWeaponComponentData.Slot primary)
    {
        OtherPlayerInstrumentActionMsg msg =
                pc.generateInstrumentActionMessage(weaponData, Instrument.Action.reload);

        msg.setDataFloat(primary.getReloadTime().asFloat(), 0);
        BrainOutServer.Controller.getClients().sendUDP(msg, client -> pc.validPlayer(client, 2));

        pushTask(new DelayTask(getStack(), primary.getReloadTime().asFloat() + 0.25f));
    }

    private void notifyAddBullet(ServerPlayerControllerComponentData pc, WeaponData weaponData, ServerWeaponComponentData.Slot primary)
    {
        OtherPlayerMagazineActionMsg msg = pc.generateMagazineActionMessage(weaponData, WeaponMagazineActionMsg.Action.loadOne);

        msg.setDataFloat(primary.getMagAddRoundTime().asFloat(), 0);
        BrainOutServer.Controller.getClients().sendUDP(msg, client -> pc.validPlayer(client, 2));

        pushTask(new DelayTask(getStack(), weaponData.getWeapon().getPrimaryProperties().getMagazineAddRoundTime()));
    }

    @Override
    protected void update(float dt)
    {
        // we need to find a weapon

        if (!validate(dt))
        {
            pop();
        }
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
