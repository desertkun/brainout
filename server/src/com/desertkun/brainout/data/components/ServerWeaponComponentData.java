package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.InstrumentEffectMsg;
import com.desertkun.brainout.common.msg.server.WeaponInfoMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.ServerBotWeaponComponent;
import com.desertkun.brainout.components.WeaponDurabilityComponent;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.ServerWeaponComponent;
import com.desertkun.brainout.content.components.interfaces.AffectedByUpgrades;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.OwnerChangedEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.utils.MisfireUtils;
import com.desertkun.brainout.utils.SharedValue;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerWeaponComponent")
@ReflectAlias("data.components.ServerWeaponComponentData")
public class ServerWeaponComponentData extends Component<ServerWeaponComponent> implements AffectedByUpgrades
{
    private final WeaponData weaponData;
    private final ServerWeaponComponent weaponComponent;
    private ObjectMap<String, Slot> slots;

    public static class Slot
    {
        private final WeaponData weaponData;
        private final ServerWeaponComponent weaponComponent;
        private final String slot;
        private IntMap<Magazine> magazines;
        private int reloadBuffer;
        private int reloadBufferQuality;

        public static class Magazine
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

        private Magazine attachedMagazine;

        private int chambered;
        private int chamberedQuality;
        private State state;
        private SharedValue<Integer> stuckIn;
        private SharedValue<Integer> misfireIn;
        private SharedValue.SimpleContainer<Integer> ownStuckIn;
        private Weapon.WeaponProperties weaponProperties;

        public enum State
        {
            normal,
            stuck,
            reloading,
            pullRequired,
            buildingUp,
            builtUp,
            fetching,
            addRound
        }

        private UpgradableProperty clipSize;
        private UpgradableProperty silent;
        private UpgradableProperty damage;
        private UpgradableProperty bullet;
        private UpgradableProperty reloadTime;
        private UpgradableProperty fetchTime;
        private UpgradableProperty reloadBothTimer;
        private UpgradableProperty cockTime;
        private UpgradableProperty fireRate;
        private UpgradableProperty fireRateB2;
        private UpgradableProperty speedCoef;
        private UpgradableProperty wearResistance;
        private UpgradableProperty magazinesCount;
        private UpgradableProperty magAddRoundTime;
        private boolean dirty;
        private boolean dirtyTcp;
        private boolean forceReset;
        private boolean loadedFirstTime;
        private boolean aggressive;
        private float fireTimer;
        private float reloadTimer;
        private float buildUpTimer;

        private Client originalOwner;

        public enum LaunchResult
        {
            success,
            wait,
            failed,
            pullRequired,
            stuck,
            misfire
        }

        public UpgradableProperty getMagAddRoundTime()
        {
            return magAddRoundTime;
        }

        public Slot(WeaponData weaponData,
                    ServerWeaponComponent weaponComponent,
                    Weapon.WeaponProperties properties,
                    String slot)
        {
            WeaponData.WeaponLoad load = weaponData.getLoad(slot);

            if (properties.hasMagazineManagement() &&
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

            this.weaponData = weaponData;
            this.weaponComponent = weaponComponent;
            this.weaponProperties = properties;
            this.stuckIn = new SharedValue<>();
            this.ownStuckIn = new SharedValue.SimpleContainer<>();
            this.dirty = false;
            this.state = State.normal;
            this.slot = slot;
            this.reloadBuffer = 0;
        }

        boolean switchSource(PlayerOwnerComponent poc)
        {
            unload(poc);
            return load(poc, false, false);
        }

        private void playEffect(String effect)
        {
            if (weaponData != null)
            {
                String effectName = weaponComponent.getEffects().get(effect);

                if (effectName != null)
                {
                    BrainOutServer.Controller.getClients().sendUDP(new InstrumentEffectMsg(
                        weaponData.getOwner(), weaponData, effectName
                    ));
                }
            }
        }

        protected String getSlot()
        {
            return slot;
        }

        public void init()
        {
            Array<Upgrade> upgrades = null;

            if (slot.equals(Constants.Properties.SLOT_PRIMARY))
            {
                upgrades = weaponData.getInfo().upgrades.values().toArray();
            }

            Weapon weapon = weaponData.getWeapon();

            initUpgrades(upgrades);

            UpgradableProperty resistance = weaponProperties.property(Constants.Properties.WEAR_RESISTANCE);
            if (weapon.hasComponent(WeaponDurabilityComponent.class) && resistance != null)
            {
                wearResistance = new UpgradableProperty(resistance, upgrades);
            }


            if (magazines != null && magazines.size == 0)
            {
                magazines = new IntMap<>();

                for (int i = 0; i < magazinesCount.asInt(); i++)
                {
                    magazines.put(i, new Magazine());
                }
            }

            if (disableWear())
            {
                stuckIn = null;
            }
            else
            {
                if (weaponData.getOwner() != null)
                {
                    originalOwner = BrainOutServer.Controller.getClients().get(weaponData.getOwner().getOwnerId());
                    if (originalOwner != null)
                    {
                        stuckIn.init(weapon.getID(), originalOwner.getStuckContainer());
                    }
                }
            }

            if (forceLoad() || (BrainOutServer.Settings.isWeaponAutoLoad() && !loadedFirstTime))
            {
                if (load(true, false))
                {
                    if (weaponProperties.hasChambering())
                    {
                        fetch(false);
                    }
                }

                loadedFirstTime = true;
            }

            if (weaponProperties.isUnlimited())
            {
                setRounds(1, -1);
                updateWeapon(true);
            }
        }

        private void initUpgrades(Array<Upgrade> upgrades)
        {
            clipSize = new UpgradableProperty(weaponProperties.property(Constants.Properties.CLIP_SIZE), upgrades);
            silent = new UpgradableProperty(weaponProperties.property(Constants.Properties.SILENT), upgrades);
            bullet = new UpgradableProperty(weaponProperties.property(Constants.Properties.BULLET), upgrades);
            reloadTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.RELOAD_TIME), upgrades);
            fetchTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.FETCH_TIME), upgrades);
            reloadBothTimer = new UpgradableProperty(weaponProperties.property(Constants.Properties.RELOAD_TIME_BOTH), upgrades);
            cockTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.COCK_TIME), upgrades);
            damage = new UpgradableProperty(weaponProperties.property(Constants.Properties.DAMAGE), upgrades);
            speedCoef = new UpgradableProperty(weaponProperties.property(Constants.Properties.SPEED_COEF), upgrades);
            fireRate = new UpgradableProperty(weaponProperties.property(Constants.Properties.FIRE_RATE), upgrades);
            fireRateB2 = new UpgradableProperty(weaponProperties.property(Constants.Properties.FIRE_RATE_B2), upgrades);
            magazinesCount = new UpgradableProperty(weaponProperties.property(Constants.Properties.NUMBER_OF_MAGAZINES), upgrades);
            magAddRoundTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.MAG_ADD_ROUND_TIME), upgrades);
        }

        private boolean forceLoad()
        {
            if (weaponProperties.isAutoLoad())
            {
                return true;
            }

            GameMode gameMode = BrainOutServer.Controller.getGameMode();

            if (gameMode != null && gameMode.getRealization() instanceof ServerRealization &&
                    ((ServerRealization) gameMode.getRealization()).forceWeaponAutoLoad())
            {
                return true;
            }

            return false;
        }

        public void upgraded(ObjectMap<String, Upgrade> upgrades)
        {
            initUpgrades(upgrades.values().toArray());
        }

        private static boolean disableWear()
        {
            return BrainOutServer.Controller.isFreePlay() ||
                BrainOutServer.Controller.isLobby();
        }

        private float getShootTime()
        {
            return (60.0f / fireRate.asFloat()) * Constants.Weapons.SHOOT_SPEED_COEF;
        }

        private float getShootTimeB2()
        {
            return (60.0f / fireRateB2.asFloat()) * Constants.Weapons.SHOOT_SPEED_COEF;
        }

        private boolean load(boolean full, boolean loadTime)
        {
            PlayerOwnerComponent poc = weaponData.getOwner().getComponent(PlayerOwnerComponent.class);
            return poc != null && load(poc, full, loadTime);
        }

        public boolean isSilent()
        {
            return silent.asBoolean();
        }

        public float getDamage()
        {
            return damage.asFloat();
        }

        public boolean buildUp()
        {
            if (weaponProperties.hasBuildUp())
            {
                buildUpTimer = weaponProperties.getBuildUpTime() * 0.5f;
                state = State.buildingUp;
                return true;
            }

            return false;
        }

        public UpgradableProperty getMagazinesCount()
        {
            return magazinesCount;
        }

        public boolean loadBoth(PlayerOwnerComponent playerOwnerComponent, boolean full, boolean loadTime)
        {
            //if (state == State.reloading)
            //    return false;

            if (loadTime)
            {
                state = State.reloading;
                reloadTimer = getReloadBothTime().asFloat();
            }
            else
            {
                state = State.normal;
            }

            fixStuck();

            if (weaponProperties.isPerBulletReload() && !full)
            {
                if (this.getRounds() >= clipSize.asInt())
                {
                    return false;
                }

                ConsumableContainer.AcquiredConsumables add = playerOwnerComponent.getAmmo(1, getBullet());

                setRounds(getRounds() + add.amount,
                    (getRoundsQuality() * getRounds() + add.amount * add.quality) / (getRounds() + add.amount));
            }
            else
            {
                if (getRounds() > 0)
                {
                    unload(playerOwnerComponent);
                }

                ConsumableContainer.AcquiredConsumables g = playerOwnerComponent.getAmmo(clipSize.asInt(), getBullet());
                setRounds(g.amount, g.quality);
            }

            updateWeapon(true);

            return true;
        }

        public boolean fetch(boolean loadTime)
        {
            if (state == State.reloading)
            {
                // if we only have left 0.2 sec of reloading allow fetch early to mitigate network issues
                if (reloadTimer >= 0.2f)
                {
                    return false;
                }
            }

            if (loadTime)
            {
                state = State.fetching;
                reloadTimer = getFetchTime().asFloat();
            }
            else
            {
                state = State.normal;
            }

            fixStuck();

            if (getChambered() >= weaponProperties.getChambers())
            {
                return true;
            }

            if (this.getRounds() > 0)
            {
                int take = Math.min(this.getRounds(), weaponProperties.getChambers());
                setChambered(take, getRoundsQuality());
                setRounds(getRounds() - take, getChamberedQuality());
                updateWeapon(true);
                return true;
            }

            return false;
        }


        public boolean loadMagazineBullet(PlayerOwnerComponent poc, int magazineId)
        {
            if (magazines == null)
            {
                return false;
            }

            Magazine mag = magazines.get(magazineId);
            if (mag == null)
            {
                return false;
            }

            if (mag.rounds >= clipSize.asInt())
            {
                return false;
            }

            switch (state)
            {
                case normal:
                case pullRequired:
                {
                    break;
                }
                case addRound:
                {
                    if (fireTimer >= 0.2f)
                    {
                        return false;
                    }
                    break;
                }
                default:
                {
                    return false;
                }
            }

            ConsumableContainer.AcquiredConsumables add = poc.getAmmo(1, getBullet());

            if (add.amount <= 0)
            {
                return false;
            }

            mag.quality = (mag.quality * mag.rounds + add.amount * add.quality) / (mag.rounds + add.amount);
            mag.rounds += add.amount;
            fireTimer = magAddRoundTime.asFloat();
            state = State.addRound;
            updateWeapon(true);
            return true;
        }

        public boolean unloadMagazineBullets(PlayerOwnerComponent poc, int magazineId)
        {
            if (magazines == null)
            {
                return false;
            }

            Magazine mag = magazines.get(magazineId);
            if (mag == null)
            {
                return false;
            }

            if (mag.rounds <= 0)
            {
                return false;
            }

            poc.putAmmo(mag.rounds, getBullet(), mag.quality);
            mag.rounds = 0;

            updateWeapon(true);
            return true;
        }

        public boolean load(PlayerOwnerComponent playerOwnerComponent, boolean full, boolean loadTime)
        {
            //if (state == State.reloading)
            //    return false;

            if (loadTime)
            {
                state = State.reloading;
                reloadTimer = getReloadTime().asFloat();
            }
            else
            {
                state = State.normal;
            }

            fixStuck();

            if (weaponProperties.isPerBulletReload() && !full)
            {
                if (this.getRounds() >= clipSize.asInt())
                {
                    return false;
                }

                ConsumableContainer.AcquiredConsumables add = playerOwnerComponent.getAmmo(1, getBullet());

                setRounds(getRounds() + add.amount,
                    (getRoundsQuality() * getRounds() + add.amount * add.quality) / (add.amount + getRounds()));
            }
            else
            {
                if (magazines != null)
                {
                    int detached = detachMagazine();
                    attachBestLoadedMagazine(detached);
                }
                else
                {
                    if (getRounds() > 0)
                    {
                        unload(playerOwnerComponent);
                    }

                    ConsumableContainer.AcquiredConsumables b =
                        playerOwnerComponent.getAmmo(clipSize.asInt(), getBullet());
                    reloadBuffer = b.amount;
                    reloadBufferQuality = b.quality;

                    setRounds(reloadBuffer, reloadBufferQuality);
                }
            }

            updateWeapon(true);

            return true;
        }

        public void checkCancelReloading()
        {
            if (state == State.reloading)
            {
                if (!weaponProperties.isPerBulletReload())
                {
                    if (getRounds() > 0)
                    {
                        setRounds(0, -1);
                    }

                    if (reloadBuffer > 0)
                    {
                        ActiveData activeData = getWeaponData().getOwner();
                        if (!(activeData instanceof PlayerData))
                            return;

                        PlayerData playerData = (PlayerData) activeData;

                        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
                        if (poc != null)
                        {
                            poc.putAmmo(reloadBuffer, getBullet(), reloadBufferQuality);
                        }
                    }

                    updateWeapon(true);
                }

                state = State.normal;
            }
        }

        private void fixStuck()
        {
            if (stuckIn != null)
            {
                if (this.stuckIn.get(0) <= 0)
                {
                    this.stuckIn.set(generateStuckIn());
                }
            }
        }

        private int getStuckIn()
        {
            if (this.stuckIn == null)
                return -1;

            return this.stuckIn.get(0);
        }

        private boolean decStuckIn()
        {
            if (stuckIn == null)
                return false;

            int value = getStuckIn();
            value--;
            stuckIn.set(value);
            return value <= 0;
        }

        private boolean testStuck()
        {
            if (stuckIn == null)
                return false;

            int stuckIn = getStuckIn();
            return stuckIn > 0 && decStuckIn();
        }

        public boolean isLoadingRoundsInMagazine()
        {
            return state == State.addRound;
        }

        private int generateStuckIn()
        {
            WeaponDurabilityComponentData wdcd = weaponData.getComponent(WeaponDurabilityComponentData.class);

            if (wdcd != null)
            {
                if (wearResistance != null && wearResistance.asFloat() != 0)
                {

                    float wear = 1.0f / wearResistance.asFloat();

                    int from = (int) (MathUtils.random(Constants.Weapons.StuckCoefficients.BAD_MIN,
                            Constants.Weapons.StuckCoefficients.BAD_MAX) / wear),
                            to = (int) (MathUtils.random(Constants.Weapons.StuckCoefficients.GOOD_MIN,
                                    Constants.Weapons.StuckCoefficients.GOOD_MAX) / wear);

                    return (int) MathUtils.lerp(from, to, wdcd.getDurabilityNormalized());
                }
            }

            return 0;
        }

        public void update(float dt)
        {
            if (fireTimer > 0)
            {
                fireTimer = Math.max(fireTimer - dt, 0);
            }

            if (dirty)
            {
                dirty = false;

                ServerBotWeaponComponent bot = weaponData.getComponent(ServerBotWeaponComponent.class);

                if (bot != null)
                {
                    WeaponSlotComponent slot = bot.getSlot(getSlot());

                    if (slot != null)
                    {
                        slot.updateInfo(getRounds(), getRoundsQuality(), getChambered(), getChamberedQuality(), forceReset, -1);
                    }
                }
                else
                {

                    ActiveData playerData = weaponData.getOwner();
                    if (playerData != null)
                    {
                        Client client = BrainOutServer.Controller.getClients().get(playerData.getOwnerId());

                        if (client instanceof PlayerClient)
                        {
                            final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                            if (poc != null)
                            {
                                ConsumableRecord record = poc.findRecord(weaponData);

                                if (record != null)
                                {
                                    int stuckIn = getStuckIn();

                                    WeaponInfoMsg msg = new WeaponInfoMsg(
                                        record, getSlot(),
                                        getRounds(), getRoundsQuality(), getChambered(), getChamberedQuality(),
                                        forceReset, stuckIn);

                                    forceReset = false;

                                    if (magazines != null)
                                    {
                                        msg.setMagazinesCount(magazines.size);
                                        int i = 0;

                                        for (IntMap.Entry<Magazine> entry : magazines)
                                        {
                                            msg.magazines[i++] = new WeaponInfoMsg.MagazineInfo(
                                                entry.key, entry.value.rounds, entry.value.quality
                                            );
                                        }
                                    }

                                    if (dirtyTcp)
                                    {
                                        dirtyTcp = false;
                                        ((PlayerClient) client).sendTCP(msg);
                                    }
                                    else
                                    {
                                        ((PlayerClient) client).sendUDP(msg);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            switch (state)
            {
                case addRound:
                {
                    fireTimer -= dt;

                    if (fireTimer <= 0)
                    {
                        fireTimer = 0;
                        state = State.normal;
                    }

                    break;
                }
                case reloading:
                case fetching:
                {
                    reloadTimer -= dt;

                    if (reloadTimer <= getThreshold())
                    {
                        reloadTimer = 0;

                        state = State.normal;
                    }
                    break;
                }
                case buildingUp:
                {
                    buildUpTimer -= dt;

                    if (buildUpTimer <= 0)
                    {
                        buildUpTimer = 0;
                        state = State.builtUp;
                    }
                    break;
                }
            }
        }

        private float getThreshold()
        {
            if (aggressive)
            {
                return  0;
            }
            else
            {
                return  0.2f;
            }
        }

        public boolean unload(PlayerOwnerComponent playerOwnerComponent)
        {
            if (state == State.reloading)
                return false;

            if (magazines != null)
            {
                detachMagazine();
                updateWeapon(true);
                return true;
            }

            if (bullet != null && getRounds() > 0)
            {
                playerOwnerComponent.putAmmo(getRounds(), getBullet(), getRoundsQuality());
                setRounds(0, -1);

                updateWeapon(true);
                return true;
            }

            return false;
        }

        public LaunchResult launch(float[] angles, int bulletsAmount, int random)
        {
            switch (state)
            {
                case fetching:
                case buildingUp:
                {
                    return LaunchResult.wait;
                }
                case pullRequired:
                {
                    return LaunchResult.pullRequired;
                }
                case reloading:
                {
                    if (getChambered() == 0)
                    {
                        return LaunchResult.wait;
                    }
                    // notice there is no break here
                }
                case normal:
                {
                    if (weaponProperties.hasBuildUp())
                    {
                        return LaunchResult.wait;
                    }
                    // notice there is no break here
                }
                case builtUp:
                {
                    float shootTime = Math.min(getShootTime(), getShootTimeB2());

                    if (bullet == null || angles == null)
                    {
                        return LaunchResult.failed;
                    }

                    if (getBullet().getAmountPerLaunch() * weaponProperties.getBulletAtLaunch() < angles.length)
                    {
                        return LaunchResult.failed;
                    }

                    if (aggressive)
                    {
                        if (fireTimer > shootTime * 0.05f)
                        {
                            return LaunchResult.wait;
                        }
                    }
                    else
                    {
                        if (fireTimer > shootTime * 0.5f)
                        {
                            aggressive = true;
                            return LaunchResult.wait;
                        }
                    }

                    boolean chambering = weaponProperties.hasChambering();
                    boolean enough = chambering ? getChambered() > 0 : getRounds() > 0;

                    if (enough)
                    {
                        if (BrainOutServer.Controller.isFreePlay() && MisfireUtils.TestMisfire(getChamberedQuality(), random))
                        {
                            setChambered(0, -1);
                            state = State.normal;
                            forceReset = true;
                            updateWeapon(true);
                            playEffect(Constants.Effects.STUCK_EFFECT);
                            return LaunchResult.misfire;
                        }
                        else if (testStuck())
                        {
                            state = State.stuck;
                            updateWeapon(true);
                            return LaunchResult.stuck;
                        }
                        else
                        {
                            fireTimer += shootTime;

                            if (chambering)
                            {
                                if (getRounds() > 0 && !weaponProperties.isPullRequired())
                                {
                                    int take = Math.min(weaponProperties.getChambers(), Math.min(bulletsAmount, getRounds()));
                                    setRounds(getRounds() - take, getRoundsQuality());
                                    setChambered(take, getRoundsQuality());
                                }
                                else
                                {
                                    setChambered(0, -1);
                                }
                            }
                            else
                            {
                                setRounds(getRounds() - bulletsAmount, getRoundsQuality());
                            }

                            applyWear();

                            if (getRounds() == 0)
                            {
                                playEffect(Constants.Effects.LAST_ROUND_EFFECT);
                            }

                            if (weaponProperties.isPullRequired())
                            {
                                state = State.pullRequired;
                            }
                        }

                        updateWeapon(false);

                        return LaunchResult.success;
                    }

                    break;
                }
                case stuck:
                {
                    playEffect(Constants.Effects.STUCK_EFFECT);

                    return LaunchResult.stuck;
                }
            }

            return LaunchResult.failed;
        }

        private boolean isSameOwner()
        {
            if (weaponData.getOwner() == null || originalOwner == null)
                return false;

            return weaponData.getOwner().getOwnerId() == originalOwner.getId();
        }

        private void applyWear()
        {
            if (disableWear())
                return;

            WeaponDurabilityComponentData wdcd = weaponData.getComponent(WeaponDurabilityComponentData.class);

            if (wdcd != null)
            {
                if (wearResistance != null && wearResistance.asFloat() != 0)
                {
                    float wear = 1.0f / wearResistance.asFloat();
                    wdcd.decreaseDurability(wear);

                    if (isSameOwner())
                    {
                        String stat = weaponData.getInstrument().getDurabilityStat();
                        float value = originalOwner.getStat(stat,
                                wdcd.getContentComponent().getDurability());

                        originalOwner.setStat(stat, Math.max(value - wear, 0));
                    }
                }
            }
        }

        public Weapon.WeaponProperties getWeaponProperties()
        {
            return weaponProperties;
        }

        public void generateNecessaryMagazines()
        {
            if (magazines == null)
                return;

            // refresh the counter
            magazinesCount =
                new UpgradableProperty(weaponProperties.property(Constants.Properties.NUMBER_OF_MAGAZINES),
                getWeaponData().getUpgrades().values());

            int have = (attachedMagazine != null ? 1 : 0) + magazines.size;

            // one extra is not counted
            int need = 1 + magazinesCount.asInt();

            for (int i = have; i < need; i++)
            {
                magazines.put(getNextMagazineId(), new Magazine());
            }

            updateWeapon(true);
        }

        public void updateWeapon(boolean tcp)
        {
            if (magazines != null)
            {
                if (isDetached())
                {
                    weaponData.setLoad(getSlot(), -1, getChambered());
                }
                else
                {
                    weaponData.setLoad(getSlot(), getRounds(), getChambered());
                }

                WeaponData.WeaponLoad load = weaponData.getLoad(getSlot());
                load.clearMagazines();

                for (IntMap.Entry<Magazine> entry : magazines)
                {
                    load.setMagazine(entry.key, entry.value.rounds, entry.value.quality);
                }
            }
            else
            {
                weaponData.setLoad(getSlot(), getRounds(), getChambered());
            }

            weaponInfoUpdated(tcp);
        }

        public void release()
        {
            ActiveData playerData = weaponData.getOwner();

            if (playerData != null && magazines == null)
            {
                PlayerOwnerComponent pog = playerData.getComponent(PlayerOwnerComponent.class);

                if (pog != null)
                {
                    unload(pog);
                }
            }

            originalOwner = null;
        }

        private void onOwnerChanged(ActiveData newOwner)
        {
            if (disableWear())
                return;

            if (newOwner != null && originalOwner != null && stuckIn != null)
            {
                boolean sameOwner = isSameOwner();
                if (!sameOwner)
                {
                    ownStuckIn.set(stuckIn.getKey(), originalOwner.getStuckContainer().get(stuckIn.getKey(), 0));
                }
                stuckIn.setContainer(sameOwner ? originalOwner.getStuckContainer() : ownStuckIn);
            }
        }

        public int getRounds()
        {
            if (weaponProperties.isUnlimited())
                return 1;

            if (attachedMagazine == null)
            {
                return -1;
            }

            return attachedMagazine.rounds;
        }

        public int getRoundsQuality()
        {
            if (weaponProperties.isUnlimited())
                return -1;

            if (attachedMagazine == null)
            {
                return -1;
            }

            return attachedMagazine.quality;
        }

        private int getNextMagazineId()
        {
            int next = 0;

            while (true)
            {
                if (magazines.containsKey(next))
                {
                    next++;
                    continue;
                }

                return next;
            }
        }

        public boolean isDetached()
        {
            return attachedMagazine == null;
        }

        public int detachMagazine()
        {
            if (magazines == null || isDetached())
                return -1;

            int nextId = getNextMagazineId();
            magazines.put(nextId, attachedMagazine);
            attachedMagazine = null;

            return nextId;
        }

        public void removeMagazine()
        {
            attachedMagazine = null;
        }

        public boolean hasMagazineManagement()
        {
            return magazines != null;
        }

        public void setMagazine(int mag, int rounds, int quality)
        {
            if (magazines == null)
            {
                return;
            }

            this.magazines.put(mag, new Magazine(rounds, quality));
        }

        public IntMap<Magazine> getMagazines()
        {
            return magazines;
        }

        public int getBestLoadedMagazine(int except)
        {
            int best = -1;
            int bestId = -1;

            if (magazines.size == 1)
            {
                except = -1;
            }

            for (IntMap.Entry<Magazine> entry : magazines)
            {
                if (entry.key == except)
                {
                    continue;
                }

                if (entry.value.rounds > best)
                {
                    best = entry.value.rounds;
                    bestId = entry.key;
                }
            }

            return bestId;
        }

        public boolean attachBestLoadedMagazine(int except)
        {
            int bestId = getBestLoadedMagazine(except);
            if (bestId == -1)
            {
                return false;
            }

            return attach(bestId);
        }

        public boolean attach(int mag)
        {
            if (!isDetached())
            {
                return false;
            }

            attachedMagazine = magazines.remove(mag);
            return true;
        }

        public void setRounds(int rounds, int quality)
        {
            if (weaponProperties.isUnlimited())
                return;

            if (attachedMagazine == null)
                return;

            attachedMagazine.rounds = rounds;
            attachedMagazine.quality = quality;
        }

        public int getChambered()
        {
            if (weaponProperties.isUnlimited())
                return weaponProperties.getChambers();

            return chambered;
        }

        public int getChamberedQuality()
        {
            if (weaponProperties.isUnlimited())
                return -1;

            return chamberedQuality;
        }

        public int getClipSize()
        {
            return clipSize.asInt();
        }

        public void setChambered(int chambered, int quality)
        {
            if (weaponProperties.isUnlimited())
                return;

            this.chambered = chambered;
            this.chamberedQuality = quality;
        }

        public WeaponData getWeaponData()
        {
            return weaponData;
        }

        public Bullet getBullet()
        {
            return bullet.asContent(Bullet.class);
        }

        public Client getOriginalOwner()
        {
            return originalOwner;
        }

        private void weaponInfoUpdated(boolean tcp)
        {
            dirty = true;
            dirtyTcp = tcp;
        }

        public UpgradableProperty getReloadTime()
        {
            return reloadTime;
        }

        public UpgradableProperty getFetchTime()
        {
            return fetchTime;
        }

        public UpgradableProperty getReloadBothTime()
        {
            return reloadBothTimer;
        }
    }

    public ServerWeaponComponentData(WeaponData weaponData, ServerWeaponComponent weaponComponent)
    {
        super(weaponData, weaponComponent);

        this.weaponData = weaponData;
        this.weaponComponent = weaponComponent;

        slots = new ObjectMap<>();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public void release()
    {
        super.release();

        for (Slot slot : slots.values())
        {
            slot.release();
        }
    }

    @Override
    public void init()
    {
        super.init();

        makeSurePrimarySlotIsCreated();
        processSecondarySlot();
    }

    public void makeSurePrimarySlotIsCreated()
    {
        addSlot(Constants.Properties.SLOT_PRIMARY, weaponData.getWeapon().getPrimaryProperties());
    }

    public void processSecondarySlot()
    {
        if (hasSlot(Constants.Properties.SLOT_SECONDARY))
            return;

        SecondaryWeaponSlotComponentData secondary =
                weaponData.getComponent(SecondaryWeaponSlotComponentData.class);

        if (secondary != null)
        {
            Weapon.WeaponProperties secondaryProperties = secondary.getWeaponProperties();

            addSlot(Constants.Properties.SLOT_SECONDARY, secondaryProperties);
        }
    }

    public boolean hasSlot(String name)
    {
        return slots.containsKey(name);
    }

    private Slot addSlot(String name, Weapon.WeaponProperties weaponProperties)
    {
        Slot slot = slots.get(name);

        if (slot != null)
        {
            return slot;
        }

        slot = new Slot(weaponData, weaponComponent, weaponProperties, name);
        slots.put(name, slot);
        slot.init();

        return slot;
    }

    @Override
    public void upgraded(ObjectMap<String, Upgrade> upgrades)
    {
        for (ObjectMap.Entry<String, Slot> slot : slots)
        {
            slot.value.upgraded(upgrades);
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        for (Slot slot : slots.values())
        {
            slot.update(dt);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case ownerChanged:
            {
                OwnerChangedEvent e = ((OwnerChangedEvent) event);

                for (Slot slot : slots.values())
                {
                    slot.onOwnerChanged(e.newOwner);
                }

                return false;
            }
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                switch (e.getAction())
                {
                    case upgradesUpdated:
                    {
                        processSecondarySlot();

                        break;
                    }
                }

                return false;
            }
        }

        return false;
    }

    public Slot getSlot(String slot)
    {
        if (slots.containsKey(slot))
        {
            return slots.get(slot);
        }

        return null;
    }

    public ObjectMap<String, Slot> getSlots()
    {
        return slots;
    }

    public static class UnloadResult
    {
        public int amount;
        public int quality;

        public UnloadResult(int amount, int quality)
        {
            this.amount = amount;
            this.quality = quality;
        }
    }

    public ObjectMap<Bullet, UnloadResult> fullWeaponUloading()
    {
        ObjectMap<Bullet, UnloadResult>  ammo = new ObjectMap();

        for (Slot slot : getSlots().values())
        {
            Bullet bullet = slot.getBullet();

            putAmmoAmountToAmmoMap(ammo, bullet, slot.getChambered(), slot.getChamberedQuality());
            slot.setChambered(0, -1);

            if (slot.attachedMagazine != null)
            {
                putAmmoAmountToAmmoMap(ammo, bullet, slot.attachedMagazine.rounds, slot.attachedMagazine.quality);
                slot.attachedMagazine.rounds = 0;
            }

            if (slot.getMagazines() != null)
            {
                for (Slot.Magazine magazine : slot.getMagazines().values())
                {
                    putAmmoAmountToAmmoMap(ammo, bullet, magazine.rounds, magazine.quality);
                    magazine.rounds = 0;
                }
            }
        }

        return ammo;
    }

    private void putAmmoAmountToAmmoMap(ObjectMap<Bullet, UnloadResult> map, Bullet bullet, int amount, int quality)
    {
        if (amount > 0)
        {
            if (map.containsKey(bullet))
            {
                UnloadResult e = map.get(bullet);

                e.quality = (e.amount * e.quality + amount * quality) / (e.amount + amount);
                e.amount += amount;
            }
            else
            {
                map.put(bullet, new UnloadResult(amount, quality));
            }

        }
    }
}