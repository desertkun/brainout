package com.desertkun.brainout.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientWeaponComponentData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.*;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.MisfireUtils;

@Reflect("content.components.ClientWeaponSlotComponent")
public class ClientWeaponSlotComponent extends WeaponSlotComponent
{
    private UpgradableProperty aimMarker;
    private String icon;

    public static class RemoteWeaponLoadSource implements WeaponLoadSource
    {
        public class Magazine
        {
            public int rounds;
            public int quality;

            public Magazine(int rounds, int quality)
            {
                this.rounds = rounds;
                this.quality = quality;
            }

            public Magazine()
            {
                this.rounds = 0;
            }
        }

        protected Magazine attachedMagazine;
        private IntMap<Magazine> magazines;
        protected int chambered;
        protected int chamberedQuality;

        public RemoteWeaponLoadSource(WeaponData weaponData, String slot, Weapon.WeaponProperties weaponProperties)
        {
            WeaponData.WeaponLoad load = weaponData.getLoad(slot);

            if (weaponProperties.hasMagazineManagement() &&
                    BrainOut.getInstance().getController().getGameMode().isMagazineManagementEnabled())
            {
                magazines = new IntMap<>();

                if (load != null && load.hasMagazines())
                {
                    for (IntMap.Entry<WeaponData.WeaponLoad.Magazine> entry : load.getMagazines())
                    {
                        magazines.put(entry.key, new Magazine(entry.value.amount, entry.value.quality));
                    }
                }
            }

            if (load != null)
            {
                if (load.isDetached())
                {
                    this.attachedMagazine = null;
                }
                else
                {
                    this.attachedMagazine = new Magazine(load.amount, load.quality);
                }

                this.chambered = load.chambered;
            }
            else
            {
                this.attachedMagazine = new Magazine();
                this.chambered = 0;
            }
        }

        @Override
        public int getMagazineStatus(int magazine)
        {
            if (magazines == null)
                return 0;

            Magazine m = magazines.get(magazine, null);

            if (m == null)
            {
                return -1;
            }

            return m.rounds;
        }

        @Override
        public void init(WeaponSlotComponent slot)
        {
            if (magazines != null && magazines.size == 0)
            {
                magazines = new IntMap<>();

                for (int i = 0; i < slot.getMagazinesCount().asInt(); i++)
                {
                    magazines.put(i, new Magazine());
                }
            }
        }

        @Override
        public boolean hasMagazine(int next)
        {
            return magazines.containsKey(next);
        }

        @Override
        public boolean isDetached()
        {
            return attachedMagazine == null;
        }

        @Override
        public void attachMagazine(int rounds, int quality)
        {
            if (!isDetached())
                return;

            attachedMagazine = new Magazine(rounds, quality);
        }

        @Override
        public boolean detachMagazine(boolean keepAttached)
        {
            if (!keepAttached)
            {
                attachedMagazine = null;
                return true;
            }

            if (magazines == null || isDetached())
                return false;

            magazines.put(getNextMagazineId(), attachedMagazine);
            attachedMagazine = null;

            return true;
        }

        @Override
        public int getRounds()
        {
            if (attachedMagazine == null)
            {
                return 0;
            }

            return attachedMagazine.rounds;
        }

        @Override
        public int getRoundsQuality()
        {
            if (attachedMagazine == null)
            {
                return -1;
            }

            return attachedMagazine.quality;
        }

        @Override
        public int getChambered()
        {
            return chambered;
        }

        @Override
        public int getChamberedQuality()
        {
            return chamberedQuality;
        }

        @Override
        public void setRounds(int rounds, int quality)
        {
            if (attachedMagazine == null)
            {
                return;
            }

            attachedMagazine.rounds = rounds;
            attachedMagazine.quality = quality;
        }

        @Override
        public void clearMagazines()
        {
            if (magazines != null)
            {
                magazines.clear();
            }
        }

        @Override
        public boolean hasMagazineManagement()
        {
            return magazines != null;
        }

        @Override
        public IntMap.Keys getMagazines()
        {
            return magazines.keys();
        }

        @Override
        public void setMagazine(int mag, int rounds, int quality)
        {
            if (magazines == null)
            {
                magazines = new IntMap<>();
            }

            magazines.put(mag, new Magazine(rounds, quality));
        }

        @Override
        public void setChambered(int chambered, int quality)
        {
            this.chambered = chambered;
            this.chamberedQuality = quality;
        }

        @Override
        public int getMagazinesCount()
        {
            if (this.magazines == null)
                return 0;

            return this.magazines.size;
        }

        private int getNextMagazineId()
        {
            int next = 0;

            while (true)
            {
                if (hasMagazine(next))
                {
                    next++;
                    continue;
                }

                return next;
            }
        }
    }

    public ClientWeaponSlotComponent(WeaponData weaponData, ConsumableRecord record,
        Weapon.WeaponProperties properties, String slot, String icon, WeaponSlotComponent.GetOtherSlot getOtherSlot)
    {
        super(weaponData, record, properties, slot,
            new RemoteWeaponLoadSource(weaponData, slot, properties), getOtherSlot);

        this.icon = icon;
    }

    public String getIcon()
    {
        return icon;
    }

    @Override
    protected void initProperties(Array<Upgrade> upgrades)
    {
        super.initProperties(upgrades);

        aimMarker = new UpgradableProperty(getWeaponProperties().property(Constants.Properties.AIM_MARKER), upgrades);
    }

    @Override
    protected int preferableShootMode(Array<Weapon.ShootMode> shootModes)
    {
        String weaponId = getData().getWeapon().getID();
        Weapon.ShootMode pref = BrainOutClient.ClientController.getUserProfile().getPreferableShootMode(weaponId);
        int index = shootModes.indexOf(pref, false);
        return index >= 0 ? index : 0;
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
            InstrumentActionEvent.obtain(action, getFetchTime().asFloat(), 0));
    }

    @Override
    protected void onReset(boolean resetHandAnimations)
    {
        Instrument.Action action = Instrument.Action.reset;

        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
                InstrumentActionEvent.obtain(action, getCockTime().asFloat(), 0));

        if (resetHandAnimations)
        {
            if (getData().getOwner() != null)
            {
                ClientPlayerComponent cpc = getData().getOwner().getComponent(ClientPlayerComponent.class);
                if (cpc != null)
                {
                    cpc.customAnimationComplete();
                }
            }
        }
    }

    @Override
    protected void onLoadRound(int magazine)
    {
        BrainOutClient.ClientController.sendTCP(
            new WeaponMagazineActionMsg(getRecord(), WeaponMagazineActionMsg.Action.loadOne,
                getSlot(), magazine));



        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
            InstrumentActionEvent.obtain(Instrument.Action.loadMagazineRound,
            getAddMagazineRoundTime().asFloat(), 0));
    }

    @Override
    protected void onUnloadRounds(int magazine)
    {
        BrainOutClient.ClientController.sendTCP(
            new WeaponMagazineActionMsg(getRecord(), WeaponMagazineActionMsg.Action.unloadAll,
                getSlot(), magazine));
    }

    @Override
    protected void onStopLoadRounds(int magazine)
    {
        MyPlayerComponent mpc = getData().getOwner().getComponent(MyPlayerComponent.class);
        if (mpc != null)
        {
            mpc.switchInstrument(getRecord(), null);
        }
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

        BrainOutClient.ClientController.sendTCP(
            new WeaponActionMsg(getRecord(), WeaponActionMsg.Action.cock, getSlot()));
    }

    private LaunchData getPlayerData()
    {
        if (getData().getOwner() != null)
        {
            return getData().getOwner().getComponent(PlayerAnimationComponentData.class).getPrimaryLaunchData();
        }

        return null;
    }

    @Override
    protected void onLaunch(LaunchData launchData, int bullets, int random)
    {
        ActiveData owner = getData().getOwner();

        if (owner == null)
            return;

        boolean silent = false;

        ClientWeaponComponentData cwcd = getData().getComponent(ClientWeaponComponentData.class);
        if (cwcd != null)
        {
            silent = cwcd.isSilent();
        }

        Bullet.BulletSlot slot = Bullet.BulletSlot.valueOf(getSlot());

        if (angles == null)
            return;

        if (BrainOutClient.ClientController.isFreePlay() && MisfireUtils.TestMisfire(getChamberedQuality(), random))
        {
            effect(Constants.Effects.STUCK_EFFECT);
        }
        else
        {
            BrainOut.EventMgr.sendDelayedEvent(owner,
                LaunchBulletEvent.obtain(getBullet(), owner.getOwnerId(), slot,
                    launchData.getX(), launchData.getY(), angles, silent));
        }

        BrainOutClient.ClientController.sendUDP(
            new BulletLaunchMsg(launchData, owner, angles, getRecord(), getBullet(),
                slot, bullets, random));
    }

    @Override
    protected void onLoadMagazine()
    {
        BrainOutClient.ClientController.sendTCP(
            new WeaponActionMsg(getRecord(), WeaponActionMsg.Action.load, getSlot()));
    }

    @Override
    protected void onFetchRounds()
    {
        BrainOutClient.ClientController.sendTCP(
            new WeaponActionMsg(getRecord(), WeaponActionMsg.Action.fetch, getSlot()));
    }

    @Override
    protected void onLoadRoundsBoth(WeaponSlotComponent otherSlot)
    {
        BrainOutClient.ClientController.sendTCP(
            new WeaponActionMsg(getRecord(), WeaponActionMsg.Action.loadBoth,
                getSlot(), otherSlot.getSlot()));
    }

    @Override
    protected void onUnloadMagazine()
    {
        BrainOutClient.ClientController.sendTCP(
            new WeaponActionMsg(getRecord(), WeaponActionMsg.Action.unload, getSlot()));
    }

    @Override
    protected boolean isBot()
    {
        return false;
    }

    @Override
    protected void onStuck()
    {
        BrainOutClient.EventMgr.sendDelayedEvent(OnScreenMessagesEvent.obtain(
            L.get("MENU_WEAPON_STUCK"), 1.0f, true
        ));
    }

    @Override
    protected void onWeaponEffect(String effect)
    {
        ActiveData playerData = getData().getOwner();

        if (playerData != null)
        {
            BrainOutClient.ClientController.sendUDP(new ClientInstrumentEffectMsg(
                playerData, getData(), effect
            ));
        }

        BrainOut.EventMgr.sendDelayedEvent(getData(), CustomInstrumentEffectEvent.obtain(effect));
    }

    @Override
    protected void onBuildUp()
    {
        BrainOutClient.ClientController.sendTCP(new WeaponActionMsg(getRecord(),
            WeaponActionMsg.Action.buildUp, getSlot()));

        BrainOut.EventMgr.sendDelayedEvent(getData().getOwner(),
            InstrumentActionEvent.obtain(Instrument.Action.buildUp));
    }

    @Override
    protected void onUpdateState()
    {
        ActiveData playerData = getData().getOwner();

        BrainOut.EventMgr.sendDelayedEvent(WeaponStateUpdatedEvent.obtain(
            playerData, getData(), this, getState()
        ));
    }

    @Override
    protected void onBeforeLaunch()
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        ActiveData playerData = getData().getOwner();

        ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);
        ClientWeaponComponentData cwcd =
                getData().getComponent(ClientWeaponComponentData.class);

        if (cpc != null)
        {
            map.shake(cwcd.getContentComponent().getBlowBack());
        }
    }

    public UpgradableProperty getAimMarker()
    {
        return aimMarker;
    }
}
