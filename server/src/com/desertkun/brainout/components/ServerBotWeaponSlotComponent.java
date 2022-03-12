package com.desertkun.brainout.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.InstrumentEffectMsg;
import com.desertkun.brainout.common.msg.server.OtherPlayerBulletLaunch;
import com.desertkun.brainout.common.msg.server.OtherPlayerInstrumentActionMsg;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.BulletThrowableComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.InstrumentActionEvent;
import com.desertkun.brainout.events.LaunchBulletEvent;

public class ServerBotWeaponSlotComponent extends WeaponSlotComponent
{
    public static class ServerWeaponLoadSource implements WeaponLoadSource
    {
        private final ServerWeaponComponentData.Slot slot;

        public ServerWeaponLoadSource(WeaponData weaponData, String slot)
        {
            ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);
            this.slot = sw.getSlot(slot);
        }

        @Override
        public int getMagazineStatus(int magazine)
        {
            if (!hasMagazineManagement())
                return -1;

            ServerWeaponComponentData.Slot.Magazine mag = slot.getMagazines().get(magazine);
            if (mag == null)
                return -1;

            return mag.rounds;
        }

        @Override
        public void init(WeaponSlotComponent slot)
        {
        }

        @Override
        public boolean hasMagazine(int next)
        {
            return hasMagazineManagement() && slot.getMagazines().containsKey(next);
        }

        @Override
        public boolean isDetached()
        {
            return slot.isDetached();
        }

        @Override
        public void attachMagazine(int mag, int quality)
        {
            slot.attach(mag);
        }

        @Override
        public boolean detachMagazine(boolean keepAttached)
        {
            if (keepAttached)
            {
                return slot.detachMagazine() >= 0;
            }
            else
            {
                slot.removeMagazine();
                return true;
            }
        }

        @Override
        public int getRounds()
        {
            return slot.getRounds();
        }

        @Override
        public int getRoundsQuality()
        {
            return slot.getRoundsQuality();
        }

        @Override
        public int getChambered()
        {
            return slot.getChambered();
        }

        @Override
        public int getChamberedQuality()
        {
            return slot.getChamberedQuality();
        }

        @Override
        public void setRounds(int rounds, int quality)
        {
            // bots cannot 'set rounds', so this function is ignored
        }

        @Override
        public void clearMagazines()
        {
            slot.getMagazines().clear();
        }

        @Override
        public boolean hasMagazineManagement()
        {
            return slot.hasMagazineManagement();
        }

        @Override
        public IntMap.Keys getMagazines()
        {
            return slot.getMagazines().keys();
        }

        @Override
        public void setMagazine(int mag, int rounds, int quality)
        {
            // bots cannot 'set mags', so this function is ignored
        }

        @Override
        public void setChambered(int chambered, int quality)
        {
            // bots cannot 'set chambered', so this function is ignored
        }

        @Override
        public int getMagazinesCount()
        {
            return slot.getMagazines().size;
        }
    }

    public ServerBotWeaponSlotComponent(
        WeaponData weaponData, ConsumableRecord record,
        Weapon.WeaponProperties properties, String slot, WeaponSlotComponent.GetOtherSlot getOtherSlot)
    {
        super(weaponData, record, properties, slot, new ServerWeaponLoadSource(weaponData, slot), getOtherSlot);
    }

    @Override
    protected void onBeforeLaunch()
    {

    }

    @Override
    protected void onWeaponEffect(String effect)
    {
        ActiveData playerData = getData().getOwner();

        if (playerData == null)
            return;

        BrainOutServer.Controller.getClients().sendUDP(new InstrumentEffectMsg(
            playerData, getData(), effect
        ));
    }

    @Override
    protected int preferableShootMode(Array<Weapon.ShootMode> shootModes)
    {
        ActiveData playerData = getData().getOwner();

        if (playerData == null)
            return 0;
        String weaponId = getData().getWeapon().getID();

        int ownerId = playerData.getOwnerId();
        Client client = BrainOutServer.Controller.getClients().get(ownerId);
        if (!(client instanceof PlayerClient))
            return 0;
        PlayerClient playerClient = ((PlayerClient) client);

        Weapon.ShootMode pref = playerClient.getProfile().getPreferableShootMode(weaponId);

        int index = shootModes.indexOf(pref, false);
        return index >= 0 ? index : 0;
    }

    @Override
    protected void onUpdateState()
    {

    }

    @Override
    protected void onBuildUp()
    {
        ActiveData playerData = getData().getOwner();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ServerWeaponComponentData sw = getData().getComponent(ServerWeaponComponentData.class);
        ServerWeaponComponentData.Slot wslot = sw.getSlot(getSlot());

        if (wslot == null)
            return;

        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
            InstrumentActionEvent.obtain(Instrument.Action.fetch));

        weaponAction(Instrument.Action.buildUp);
        wslot.buildUp();
    }

    @Override
    protected void onStuck()
    {
        doReload(false);
    }

    @Override
    protected void onUnloadMagazine()
    {
        //
    }

    @Override
    protected boolean isBot()
    {
        if (getData() == null)
        {
            return false;
        }

        ActiveData owner = getData().getOwner();
        if (owner == null)
        {
            return false;
        }

        return owner.getComponent(BotControllerComponentData.class) != null;
    }

    @Override
    protected void onReload(Instrument.Action action)
    {
        if (action == null)
        {
            if (getSlot().equals(Constants.Properties.SLOT_PRIMARY))
            {
                action = Instrument.Action.reload;
            }
            else
            {
                action = Instrument.Action.reloadSecondary;
            }
        }

        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
            InstrumentActionEvent.obtain(action, getReloadTime().asFloat(), getFetchTime().asFloat()));

        weaponAction(action);
    }

    @Override
    protected void onFetch(Instrument.Action action)
    {
        if (action == null)
        {
            if (getSlot().equals(Constants.Properties.SLOT_PRIMARY))
            {
                action = Instrument.Action.fetch;
            }
            else
            {
                action = Instrument.Action.fetchSecondary;
            }
        }

        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
            InstrumentActionEvent.obtain(action, getReloadTime().asFloat(), getFetchTime().asFloat()));

        weaponAction(action);
    }

    private void weaponAction(Instrument.Action action)
    {
        ActiveData playerData = getData().getOwner();

        SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (cmp == null)
            return;

        PlayerComponentData pcd = playerData.getComponent(PlayerComponentData.class);
        if (pcd == null)
            return;

        if (getData() == null)
            return;

        Vector2 speed = cmp.getSpeed();

        BrainOutServer.Controller.getClients().sendTCP(
            new OtherPlayerInstrumentActionMsg(
                playerData.getId(), playerData.getX(), playerData.getY(),
                speed.x, speed.y,  playerData.getAngle(), playerData.getDimension(),
                pcd.getMousePosition().x, pcd.getMousePosition().y,
                getData(), action));
    }

    @Override
    protected void onCock()
    {
        Instrument.Action action;

        if (getSlot().equals(Constants.Properties.SLOT_PRIMARY))
        {
            action = Instrument.Action.cock;
        }
        else
        {
            action = Instrument.Action.cockSecondary;
        }

        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
                InstrumentActionEvent.obtain(action, getCockTime().asFloat(), 0));

        weaponAction(action);
    }

    @Override
    protected void onReset(boolean resetHandAnimations)
    {
        Instrument.Action action = Instrument.Action.reset;

        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
            InstrumentActionEvent.obtain(action, getCockTime().asFloat(), 0));
    }

    @Override
    protected void onLoadMagazine()
    {
        ActiveData playerData = getData().getOwner();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ServerWeaponComponentData sw = getData().getComponent(ServerWeaponComponentData.class);
        ServerWeaponComponentData.Slot wslot = sw.getSlot(getSlot());

        if (wslot == null)
            return;

        wslot.load(poc, false, true);
    }

    @Override
    protected void onLoadRound(int magazine)
    {
        ActiveData playerData = getData().getOwner();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ServerWeaponComponentData sw = getData().getComponent(ServerWeaponComponentData.class);
        ServerWeaponComponentData.Slot wslot = sw.getSlot(getSlot());

        if (wslot == null)
            return;

        wslot.loadMagazineBullet(poc, magazine);
    }

    @Override
    protected void onUnloadRounds(int magazine)
    {
        ActiveData playerData = getData().getOwner();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ServerWeaponComponentData sw = getData().getComponent(ServerWeaponComponentData.class);
        ServerWeaponComponentData.Slot wslot = sw.getSlot(getSlot());

        if (wslot == null)
            return;

        wslot.unloadMagazineBullets(poc, magazine);
    }

    @Override
    protected void onStopLoadRounds(int magazine)
    {
        ActiveData playerData = getData().getOwner();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        poc.setCurrentInstrument(getRecord());
    }

    @Override
    protected void onFetchRounds()
    {
        ActiveData playerData = getData().getOwner();
        if (playerData == null)
            return;

        ServerWeaponComponentData sw = getData().getComponent(ServerWeaponComponentData.class);
        ServerWeaponComponentData.Slot wslot = sw.getSlot(getSlot());

        if (wslot == null)
            return;

        wslot.fetch(true);
    }

    @Override
    protected void onLoadRoundsBoth(WeaponSlotComponent otherSlot)
    {
        ActiveData playerData = getData().getOwner();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ServerWeaponComponentData sw = getData().getComponent(ServerWeaponComponentData.class);
        ServerWeaponComponentData.Slot wslot = sw.getSlot(getSlot());
        ServerWeaponComponentData.Slot wslotOther = sw.getSlot(otherSlot.getSlot());

        if (wslot == null || wslotOther == null)
            return;

        wslot.loadBoth(poc, false, true);
        wslotOther.loadBoth(poc, false, true);
    }

    @Override
    protected void onLaunch(LaunchData launchAt, int bullets, int random)
    {
        Bullet bullet = getBullet();
        if (bullet == null)
            return;

        WeaponData weaponData = getData();

        ActiveData owner = getData().getOwner();
        if (!(owner instanceof PlayerData))
            return;

        PlayerData playerData = ((PlayerData) owner);

        Map map = playerData.getMap();

        ServerWeaponComponentData swcd = getData().getComponent(ServerWeaponComponentData.class);
        if (swcd == null)
            return;

        ServerWeaponComponentData.Slot wslot = swcd.getSlot(getSlot());
        if (wslot == null)
            return;

        boolean silent = wslot.isSilent();

        Bullet.BulletSlot slot = Bullet.BulletSlot.valueOf(getSlot());

        if (angles == null)
            return;

        ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

        if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
            return;

        boolean syncToOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

        BrainOut.EventMgr.sendDelayedEvent(playerData,
            LaunchBulletEvent.obtain(bullet, -1, slot,
                launchAt.getX(), launchAt.getY(), angles, silent));

        int bulletsAmount = getWeaponProperties().getBulletAtLaunch();

        switch (wslot.launch(angles, bulletsAmount, random))
        {
            case success:
            {
                BulletThrowableComponent throwableComponent =
                    bullet.getComponent(BulletThrowableComponent.class);

                for (float angle : angles)
                {
                    BulletData bulletData = bullet.getData(launchAt, wslot.getDamage(),
                            getComponentObject().getDimension());

                    if (bulletData == null)
                        return;

                    bulletData.setOwnerId(playerData.getOwnerId());
                    bulletData.setPlayerData(playerData);
                    bulletData.setInstrumentInfo(weaponData.getInfo());

                    map.addBullet(bulletData);

                    if (throwableComponent != null)
                    {
                        ThrowableActive thr = throwableComponent.getThrowActive();

                        ThrowableActiveData activeData = thr.getData(map.getDimension());

                        activeData.setPosition(launchAt.getX(), launchAt.getY());
                        activeData.setOwnerId(playerData.getOwnerId());
                        activeData.setAngle(angle);
                        activeData.setLaunchedBy(weaponData.getInfo());
                        activeData.setTeam(playerData.getTeam());

                        SimplePhysicsComponentData phy =
                                activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

                        phy.getSpeed().set(
                                throwableComponent.getThrowPower() * MathUtils.cosDeg(angle),
                                throwableComponent.getThrowPower() * MathUtils.sinDeg(angle)
                        );

                        map.addActive(map.generateServerId(), activeData, true);
                    }

                }

                if (!syncToOthers)
                {
                    // in  case of hideOthers flag, do not sync the shooting to other players
                    return;
                }

                BrainOutServer.Controller.getClients().sendUDP(new OtherPlayerBulletLaunch(playerData,
                    launchAt.getX(), launchAt.getY(),
                    playerData.getX(), playerData.getY(), angles,
                    weaponData, bullet, slot, silent));

                ServerTeamVisibilityComponentData stv =
                    playerData.getComponent(ServerTeamVisibilityComponentData.class);

                if (stv != null && !wslot.isSilent())
                {
                    stv.show();
                }
            }
        }
    }
}
