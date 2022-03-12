package com.desertkun.brainout.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.InstrumentUpgradeComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RandomLaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.InstrumentActionEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;
import com.desertkun.brainout.events.SimpleEvent;

import java.util.Random;

public abstract class WeaponSlotComponent extends Component
{
    public enum State
    {
        idle,
        coupleIdle,
        launched,
        reloading,
        reloadingBoth,
        fixed,
        burst,
        stuck,
        stuckIdle,
        cocking,
        cocked,
        empty,
        buildUp,
        fetching,
        fetchWait,
        misfireWait,
        loadMagazineRound
    }

    public enum ActionAfterPull
    {
        none,
        reload,
        unload
    }

    private final String slot;
    private final GetOtherSlot getOtherSlot;

    private Random random = new Random();

    private UpgradableProperty aimDistance;
    private UpgradableProperty damage;
    private UpgradableProperty bullet;
    private UpgradableProperty accuracy;
    private UpgradableProperty recoil;
    private UpgradableProperty reloadTime;
    private UpgradableProperty fetchTime;
    private UpgradableProperty reloadBothTime;
    private UpgradableProperty cockTime;
    private UpgradableProperty fireRate, fireRateB2;
    private UpgradableProperty clipSize;
    private UpgradableProperty magazinesCount;
    private UpgradableProperty wearResistance;
    private UpgradableProperty speedCoef;
    private UpgradableProperty shootModes;
    private UpgradableProperty addMagazineRoundTime;

    protected State state;
    protected double timer;
    protected boolean launching;
    protected float angles[];
    protected int shootMode;
    private int burstCounter;
    private int shootSequence;
    private int launchesToStuck;
    private boolean forceReset;
    private int loadMagazineId;
    private float currentAccuracy;
    private float angleOffset;

    private final ConsumableRecord record;
    private final WeaponData data;
    private final Weapon.WeaponProperties weaponProperties;
    private boolean builtUp;
    private WeaponLoadSource source;

    public interface WeaponLoadSource
    {
        int getMagazineStatus(int magazine);
        void init(WeaponSlotComponent slot);
        boolean hasMagazine(int next);
        boolean isDetached();
        void attachMagazine(int rounds, int quality);
        boolean detachMagazine(boolean keepAttached);
        int getRounds();
        int getRoundsQuality();
        int getChambered();
        int getChamberedQuality();
        void setRounds(int rounds, int quality);
        void clearMagazines();
        boolean hasMagazineManagement();
        IntMap.Keys getMagazines();
        void setMagazine(int mag, int rounds, int quality);
        void setChambered(int chambered, int quality);
        int getMagazinesCount();
    }

    public interface GetOtherSlot
    {
        WeaponSlotComponent get(String name);
    }

    public WeaponSlotComponent(WeaponData weaponData, ConsumableRecord record,
        Weapon.WeaponProperties properties, String slot, WeaponLoadSource weaponInfoSource, GetOtherSlot getOtherSlot)
    {
        super(weaponData, null);

        this.slot = slot;
        this.weaponProperties = properties;
        this.data = weaponData;
        this.state = State.idle;
        this.timer = 0;
        this.source = weaponInfoSource;
        this.getOtherSlot = getOtherSlot;

        this.currentAccuracy = 0;
        this.angleOffset = 0;
        this.shootSequence = 0;
        this.launchesToStuck = -1;
        this.record = record;
        this.angles = null;

        this.burstCounter = 0;

        Array<Upgrade> upgrades = null;

        if (slot.equals(Constants.Properties.SLOT_PRIMARY))
        {
            upgrades = data.getInfo().upgrades.values().toArray();

            ActiveData owner = weaponData.getOwner();
            if (owner != null)
            {
                InstrumentUpgradeComponentData iucd = owner.getComponent(InstrumentUpgradeComponentData.class);
                if (iucd != null)
                {
                    InstrumentUpgradeComponent upgradeComponent = iucd.getContentComponent();

                    if (upgradeComponent.getSlot() == weaponData.getWeapon().getSlot())
                    {
                        upgrades.add(upgradeComponent.getUpgrade());
                    }
                }
            }
        }

        initProperties(upgrades);

        this.shootMode = preferableShootMode(this.shootModes.asEnumArray(Weapon.ShootMode.class));
    }

    protected abstract int preferableShootMode(Array<Weapon.ShootMode> shootModes);

    public boolean isReloading()
    {
        switch (state)
        {
            case reloading:
            case reloadingBoth:
                return true;
        }

        return false;
    }

    protected void initProperties(Array<Upgrade> upgrades)
    {
        aimDistance = new UpgradableProperty(weaponProperties.property(Constants.Properties.AIM_DISTANCE), upgrades);
        damage = new UpgradableProperty(weaponProperties.property(Constants.Properties.DAMAGE), upgrades);
        bullet = new UpgradableProperty(weaponProperties.property(Constants.Properties.BULLET), upgrades);
        accuracy = new UpgradableProperty(weaponProperties.property(Constants.Properties.ACCURACY), upgrades);
        recoil = new UpgradableProperty(weaponProperties.property(Constants.Properties.RECOIL), upgrades);
        reloadTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.RELOAD_TIME), upgrades);
        fetchTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.FETCH_TIME), upgrades);
        reloadBothTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.RELOAD_TIME_BOTH), upgrades);
        cockTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.COCK_TIME), upgrades);
        fireRate = new UpgradableProperty(weaponProperties.property(Constants.Properties.FIRE_RATE), upgrades);
        fireRateB2 = new UpgradableProperty(weaponProperties.property(Constants.Properties.FIRE_RATE_B2), upgrades);
        clipSize = new UpgradableProperty(weaponProperties.property(Constants.Properties.CLIP_SIZE), upgrades);
        speedCoef = new UpgradableProperty(weaponProperties.property(Constants.Properties.SPEED_COEF), upgrades);
        shootModes = new UpgradableProperty(weaponProperties.property(Constants.Properties.SHOOT_MODES), upgrades);
        magazinesCount = new UpgradableProperty(weaponProperties.property(Constants.Properties.NUMBER_OF_MAGAZINES), upgrades);
        addMagazineRoundTime = new UpgradableProperty(weaponProperties.property(Constants.Properties.MAG_ADD_ROUND_TIME), upgrades);

        UpgradableProperty resistance = weaponProperties.property(Constants.Properties.WEAR_RESISTANCE);
        if (data.getComponent(WeaponDurabilityComponentData.class) != null && resistance != null)
        {
            wearResistance = new UpgradableProperty(resistance, upgrades);
        }

        source.init(this);
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
    public boolean onEvent(Event event)
    {
        return false;
    }

    public Weapon.ShootMode getShootMode()
    {
        Array<Weapon.ShootMode> shootModes = this.shootModes.asEnumArray(Weapon.ShootMode.class);

        if (shootMode >= shootModes.size)
            return shootModes.first();

        return shootModes.get(shootMode);
    }

    public void setLaunching(boolean launching)
    {
        this.launching = launching;
    }

    private boolean isEnabled(boolean checkShootingDisabled)
    {
        if (data.getOwner() == null)
            return true;

        ActiveData owner = data.getOwner();

        if (owner == null)
            return false;

        Map map = getMap();

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt(((int) owner.getX()), ((int) owner.getY()));

        if (checkShootingDisabled && chunk != null)
        {
            if (chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                return false;
        }

        PlayerOwnerComponent poc = owner.getComponent(PlayerOwnerComponent.class);

        return poc == null || poc.isEnabled();
    }

    public void beginLaunching()
    {
        if (!isEnabled(true))
            return;

        setLaunching(true);
    }

    public int getMagazineStatus(int magazine)
    {
        return source.getMagazineStatus(magazine);
    }

    public int getLoadMagazineId()
    {
        return loadMagazineId;
    }

    public boolean isDetached()
    {
        return source.isDetached();
    }

    public void attachMagazine(int rounds, int quality)
    {
        source.attachMagazine(rounds, quality);
    }

    public boolean detachMagazine()
    {
        return source.detachMagazine(true);
    }

    public void endLaunching()
    {
        if (!isEnabled(true))
            return;

        setLaunching(false);
    }

    public boolean isLaunching()
    {
        return launching;
    }

    public Bullet getBullet()
    {
        return bullet.asContent(Bullet.class);
    }

    @Override
    public void update(float dt)
    {
        switch (state)
        {
            case coupleIdle:
            {
                if (isLaunching())
                {
                    if (doLaunch())
                    {
                        setState(State.idle);
                    }
                }
                else
                {
                    updateAccuracy(dt);
                }

                break;
            }
            case fetchWait:
            case misfireWait:
            {
                if (!isLaunching())
                {
                    setState(State.idle);
                }

                break;
            }
            case buildUp:
            {
                if (isLaunching())
                {
                    timer -= dt;

                    if (timer < 0)
                    {
                        builtUp = true;
                        doLaunch();
                    }
                }
                else
                {
                    timer = 0;
                    builtUp = false;
                    setState(State.idle);
                }

                break;
            }
            case idle:
            {
                if (isLaunching())
                {
                    ActiveData owner = data.getOwner();

                    if (owner == null)
                        return;

                    ActiveProgressVisualComponentData progressComponent = owner.getComponent(ActiveProgressVisualComponentData.class);

                    if (progressComponent != null && progressComponent.isActive()) {
                        return;
                    }

                    if (weaponProperties.hasBuildUp() && !builtUp)
                    {
                        if (getRounds() <= 0)
                        {
                            if (!doReload(false))
                                return;
                        }
                        else
                        {
                            buildUp();
                        }
                    }
                    else
                    {
                        switch (getShootMode())
                        {
                            case singleCock:
                            {
                                if (getRounds() <= 0)
                                {
                                    if (!doReload(false))
                                        return;
                                }

                                timer = cockTime.asFloat();
                                doCock();

                                return;
                            }
                            case burst:
                            {
                                burstCounter = 3;
                                break;
                            }
                            case burst2:
                            {
                                burstCounter = 2;
                                break;
                            }
                        }

                        doLaunch();
                    }
                }
                else
                {
                    builtUp = false;
                    shootSequence = 0;
                    updateAccuracy(dt);
                }

                break;
            }
            case cocked:
            {
                if (isLaunching())
                {
                    doLaunch();
                }
                else
                {
                    shootSequence = 0;

                    updateAccuracy(dt);
                }

                break;
            }
            case burst:
            {
                doLaunch();

                break;
            }
            case fixed:
            {
                if (!isLaunching())
                {
                    burstCounter = 0;

                    if (weaponProperties.isPullRequired() && getChambered() == 0 && getRounds() > 0)
                    {
                        doFetch();
                    }
                    else
                    {
                        setState(State.idle);
                    }
                }
                break;
            }
            case launched:
            {
                timer -= dt;
                if (timer <= 0)
                {
                    switch (getShootMode())
                    {
                        case auto:
                        {
                            if (forceReset)
                            {
                                forceReset = false;
                                setState(State.misfireWait);
                            }
                            else if (weaponProperties.isPullRequired() && getChambered() == 0 && getRounds() > 0)
                            {
                                doFetch();
                            }
                            else
                            {
                                setState(State.idle);

                                if (getRounds() == 0 && getChambered() == 0 && weaponProperties.isAutoReload())
                                {
                                    doReload(false);
                                }
                            }

                            update(dt);

                            return;
                        }

                        case singleCock:
                        {
                            setState(State.idle);

                            update(dt);

                            break;
                        }

                        case couple:
                        {
                            setState(State.coupleIdle);

                            break;
                        }

                        case single:
                        {
                            setState(State.fixed);

                            break;
                        }

                        case burst:
                        case burst2:
                        {
                            burstCounter--;

                            if (burstCounter > 0)
                            {
                                setState(State.burst);
                            }
                            else
                            {
                                setState(State.fixed);
                            }

                            break;
                        }
                    }

                    if (getRounds() == 0 && getChambered() == 0 && weaponProperties.isAutoReload())
                    {
                        if (isBot())
                        {
                            doReload(false);
                        }
                        else
                        {
                            // no auto-reload on empty mag when per-mag mgmt is there
                            if (!hasMagazineManagement())
                            {
                                doReload(false);
                            }
                        }
                    }
                }

                break;
            }
            case reloadingBoth:
            {
                shootSequence = 0;

                timer -= dt;
                if (timer <= 0)
                {
                    setState(State.idle);
                }

                updateAccuracy(dt);

                break;
            }
            case fetching:
            {
                shootSequence = 0;

                timer -= dt;
                if (timer <= 0)
                {
                    setState(State.idle);

                    if (isLaunching())
                    {
                        doLaunch();
                    }
                }

                updateAccuracy(dt);

                break;
            }
            case reloading:
            {
                shootSequence = 0;

                timer -= dt;
                if (timer <= 0)
                {
                    setState(State.idle);

                    if (weaponProperties.isPerBulletReload())
                    {
                        ActiveData owner = data.getOwner();
                        if (owner == null)
                        {
                            break;
                        }
                        
                        PlayerOwnerComponent poc = owner.getComponent(PlayerOwnerComponent.class);
                        if (poc == null)
                        {
                            break;
                        }

                        if (getRounds() < clipSize.asInt() && poc.hasAmmo(getBullet()))
                        {
                            if (isLaunching() && getRounds() > 0)
                            {
                                doLaunch();
                            }
                            else
                            {
                                doReload(false);
                            }
                        }
                        else
                        {
                            if (weaponProperties.hasChambering() && getChambered() == 0 && getRounds() > 0)
                            {
                                doFetch();
                            }
                        }
                    }
                    else
                    {
                        if (weaponProperties.hasChambering() && getChambered() == 0 && getRounds() > 0)
                        {
                            doFetch();
                        }
                    }
                }

                updateAccuracy(dt);

                break;
            }
            case loadMagazineRound:
            {
                timer -= dt;

                if (isLaunching() || !hasMagazineManagement() || !source.hasMagazine(loadMagazineId))
                {
                    doStopLoadingBullets();
                }
                else if (timer <= 0)
                {
                    int rounds = source.getMagazineStatus(loadMagazineId);

                    if (rounds < getClipSize().asInt())
                    {
                        if (hasAmmo())
                        {
                            onLoadRound(loadMagazineId);
                            timer = getAddMagazineRoundTime().asFloat();
                        }
                        else
                        {
                            doStopLoadingBullets();
                        }
                    }
                    else
                    {
                        doStopLoadingBullets();
                    }
                }
                break;
            }
            case cocking:
            {
                timer -= dt;
                if (timer <= 0)
                {
                    currentAccuracy = 0.5f;

                    if (isLaunching())
                    {
                        doLaunch();
                    }
                    else
                    {
                        setState(State.cocked);
                    }
                }

                updateAccuracy(dt);

                break;
            }
            case empty:
            {
                if (!isLaunching())
                {
                    setState(State.idle);
                }

                break;
            }
            case stuckIdle:
            {
                if (!isLaunching())
                {
                    setState(State.stuck);
                }

                break;
            }
            case stuck:
            {
                if (isLaunching())
                {
                    stuck();
                    setState(State.stuckIdle);
                }

                updateAccuracy(dt);

                break;
            }
        }
    }

    private void doCock()
    {
        if (isPlayingCustomAnimation())
            return;

        onCock();
        setState(State.cocking);
    }

    public void doReset(boolean resetHandAnimations)
    {
        if (isPlayingCustomAnimation())
            return;

        onReset(resetHandAnimations);
        setState(State.idle);
    }

    public void doResetAndHold(boolean resetHandAnimations)
    {
        onReset(resetHandAnimations);
        setState(State.fetchWait);
    }

    private boolean hasAmmo()
    {
        ActiveData ownerPlayer = getData().getOwner();
        if (ownerPlayer == null)
            return false;

        PlayerOwnerComponent owner = ownerPlayer.getComponent(PlayerOwnerComponent.class);
        if (owner == null)
            return false;

        return owner.hasAmmo(getBullet());
    }

    public void doLoadRounds(int magazineId)
    {
        if (!hasMagazineManagement())
            return;

        if (!source.hasMagazine(magazineId))
            return;

        if (!hasAmmo())
            return;

        if (isPlayingCustomAnimation())
            return;

        loadMagazineId = magazineId;
        setState(State.loadMagazineRound);
        onLoadRound(magazineId);
        timer = addMagazineRoundTime.asFloat();
    }

    public void doUnloadAllRounds(int magazineId)
    {
        if (isPlayingCustomAnimation())
            return;

        onUnloadRounds(magazineId);
    }

    public void buildUp()
    {
        setState(State.buildUp);
        timer = weaponProperties.getBuildUpTime();
        onBuildUp();
    }

    public float calculateAccuracy()
    {
        return Interpolation.sineIn.apply(
            1.0f - MathUtils.clamp(accuracy.asFloat(), 0.0f, 100.0f) / 100.0f
        );
    }

    public float calculateRecoil()
    {
        return Interpolation.exp5In.apply(
            MathUtils.clamp(recoil.asFloat(), 0.0f, 100.0f) / 100.0f
        );
    }

    public float calculateAccuracyMin()
    {
        return MathUtils.lerp(Constants.Weapon.ACCURACY_MIN_MIN,
                Constants.Weapon.ACCURACY_MIN_MAX, calculateAccuracy());
    }

    public float calculateAccuracyMax()
    {
        return MathUtils.lerp(Constants.Weapon.ACCURACY_MAX_MIN,
                Constants.Weapon.ACCURACY_MAX_MAX, calculateAccuracy());
    }

    public float calculateLaunchAdd()
    {
        return MathUtils.lerp(Constants.Weapon.RECOIL_LAUNCH_ADD_MIN,
                Constants.Weapon.RECOIL_LAUNCH_ADD_MAX, calculateRecoil());
    }

    public float calculateLaunchAngeAdd()
    {
        return MathUtils.lerp(Constants.Weapon.RECOIL_LAUNCH_ANGLE_ADD_MIN,
                Constants.Weapon.RECOIL_LAUNCH_ANGLE_ADD_MAX, calculateRecoil());
    }

    public float calculateBreakdown()
    {
        return MathUtils.lerp(Constants.Weapon.RECOIL_BREAKDOWN_MIN,
                Constants.Weapon.RECOIL_BREAKDOWN_MAX, calculateRecoil());
    }

    public void launch(LaunchData launchData, int bullets)
    {
        if (bullet == null || getBullet() == null)
            return;

        float coef = 1.0f;

        ActiveData playerData = data.getOwner();

        if (playerData != null)
        {
            SimplePhysicsComponentData pc = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            boolean hasBottomContact = pc.hasContact(SimplePhysicsComponentData.Contact.bottom);

            if (!hasBottomContact)
            {
                coef *= Constants.Weapons.SHOOT_IN_AIR_OFFSET_COEF;
            }
        }

        PlayerAnimationComponentData anim = playerData.getComponent(PlayerAnimationComponentData.class);

        if (anim != null)
        {
            float f = (MathUtils.random() - 0.25f) * calculateLaunchAngeAdd() * currentAccuracy * coef;

            if (!anim.getSkeleton().getFlipX())
            {
                angleOffset = MathUtils.clamp(angleOffset + f, 0, Constants.Weapon.RECOIL_LAUNCH_ANGLE_LIMIT);
            }
            else
            {
                angleOffset = MathUtils.clamp(angleOffset - f, -Constants.Weapon.RECOIL_LAUNCH_ANGLE_LIMIT, 0);
            }
        }

        int amount = getBullet().getAmountPerLaunch() * bullets;

        if (angles == null || angles.length != amount)
        {
            angles = new float[amount];
        }

        for (int i = 0; i < amount; i++)
        {
            float min = calculateAccuracyMin();
            float max = calculateAccuracyMax();

            float acc = min + (max - min) * currentAccuracy;

            RandomLaunchData randomLaunchData = new RandomLaunchData(launchData, random, acc);

            angles[i] = randomLaunchData.getAngle();
        }

        currentAccuracy = MathUtils.clamp(currentAccuracy + calculateLaunchAdd(), 0f, 1f);

        onLaunch(launchData, bullets, MathUtils.random(0, 100000));
    }

    abstract protected void onLaunch(LaunchData launchData, int bullets, int random);

    public boolean isLoaded()
    {
        return bullet != null && getRounds() > 0;
    }

    public int getRounds()
    {
        if (weaponProperties.isUnlimited())
            return 1;

        return source.getRounds();
    }

    public int getRoundsQuality()
    {
        if (weaponProperties.isUnlimited())
            return -1;

        return source.getRoundsQuality();
    }

    public int getChambered()
    {
        if (weaponProperties.isUnlimited())
            return weaponProperties.getChambers();

        return source.getChambered();
    }

    public int getChamberedQuality()
    {
        if (weaponProperties.isUnlimited())
            return -1;

        return source.getChamberedQuality();
    }

    public UpgradableProperty getClipSize()
    {
        return clipSize;
    }

    public void setRounds(int rounds, int quality)
    {
        if (weaponProperties.isUnlimited())
            return;

        source.setRounds(rounds, quality);
    }

    public void doStopLoadingBullets()
    {
        if (isPlayingCustomAnimation())
            return;

        onStopLoadRounds(loadMagazineId);
        stopLoadingBullets();
    }

    public void stopLoadingBullets()
    {
        doResetAndHold(true);
    }

    public void clearMagazines()
    {
        source.clearMagazines();
    }

    public IntMap.Keys getMagazines()
    {
        return source.getMagazines();
    }

    public boolean hasMagazineManagement()
    {
        return source.hasMagazineManagement();
    }

    public boolean hasMagazineAttached()
    {
        return !source.isDetached();
    }

    public void setMagazine(int mag, int rounds, int quality)
    {
        source.setMagazine(mag, rounds, quality);
    }

    public void setChambered(int chambered, int chamberedQuality)
    {
        if (weaponProperties.isUnlimited())
            return;

        source.setChambered(chambered, chamberedQuality);
    }

    public void setState(State state)
    {
        if (this.state != state)
        {
            this.state = state;
            onUpdateState();
        }
    }

    public int getBulletsToLaunch()
    {
        return MathUtils.clamp(weaponProperties.getBulletAtLaunch(), 1, getRounds());
    }

    public boolean doLaunch()
    {
        boolean haveRounds;

        if (weaponProperties.hasChambering())
        {
            haveRounds = getChambered() > 0;
        }
        else
        {
            haveRounds = getRounds() > 0;
        }

        if (haveRounds)
        {
            float tm;

            switch (getShootMode())
            {
                case couple:
                case burst:
                case burst2:
                    tm = (60.0f / fireRateB2.asFloat()) * Constants.Weapons.SHOOT_SPEED_COEF;
                    break;
                default:
                    tm = (60.0f / fireRate.asFloat()) * Constants.Weapons.SHOOT_SPEED_COEF;
                    break;
            }

            timer += tm;

            int bullets = getBulletsToLaunch();

            ActiveData playerData = data.getOwner();
            shootSequence += 1;

            if (playerData != null)
            {
                onBeforeLaunch();
            }

            if (applyWear())
            {
                if (weaponProperties.hasChambering())
                {
                    bullets = Math.max(bullets, getChambered());
                    setChambered(0, -1);
                }
                else
                {
                    setRounds(getRounds() - bullets, getRoundsQuality());
                }

                WeaponAnimationComponentData wac = data.getComponent(WeaponAnimationComponentData.class);

                if (wac != null)
                {
                    launch(wac.getLaunchPointData(), bullets);
                    setState(State.launched);
                }

                if (weaponProperties.hasChambering() && getRounds() > 0 && !isReloading())
                {
                    if (weaponProperties.isPullRequired())
                    {
                        setState(State.fetchWait);
                    }
                    else
                    {
                        int take = Math.min(getRounds(), weaponProperties.getChambers());
                        setRounds(getRounds() - take, getRoundsQuality());
                        setChambered(take, getChamberedQuality());
                    }
                }

                return true;
            }
            else
            {
                stuck();

                setState(State.stuckIdle);

                return false;
            }

        }
        else
        {
            boolean fetch;

            if (isBot())
            {
                fetch = true;
            }
            else
            {
                if (weaponProperties.hasChambering())
                {
                    fetch = getRounds() > 0;
                }
                else
                {
                    fetch = false;
                }
            }

            if (fetch)
            {
                doFetch();
            }
            else
            {
                // no auto-reload on empty mag when per-mag mgmt is there
                if (!doReload(false))
                {
                    empty();
                    setState(State.empty);
                }
            }

            return false;
        }
    }

    public void nextShootMode()
    {
        Array<Weapon.ShootMode> shootModes = this.shootModes.asEnumArray(Weapon.ShootMode.class);

        shootMode++;

        if (shootMode >= shootModes.size)
        {
            shootMode = 0;
        }

        WeaponAnimationComponentData cwcd = getData().getComponent(WeaponAnimationComponentData.class);

        BrainOut.EventMgr.sendDelayedEvent(getData(),
                LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.switchMode, cwcd.getLaunchPointData()));
    }

    protected abstract void onBeforeLaunch();

    protected void effect(String name)
    {
        onWeaponEffect(name);
    }

    protected abstract void onWeaponEffect(String effect);

    private void empty()
    {
        effect(Constants.Effects.EMPTY_EFFECT);
    }

    private void stuck()
    {
        effect(Constants.Effects.STUCK_EFFECT);

        onStuck();
    }

    public boolean doUnload()
    {
        if (!isEnabled(false))
            return false;

        if (!weaponProperties.canUnload())
        {
            return false;
        }

        if (isPlayingCustomAnimation())
        {
            return false;
        }

        releaseRounds();
        reload();

        return true;
    }

    public void doFetch()
    {
        if (!isEnabled(false))
            return;

        if (state == State.fetching ||
            state == State.reloading)
            return;

        if (data.getOwner() == null)
            return;

        if (isPlayingCustomAnimation())
            return;

        boolean fineToFetch = getRounds() > 0 && getChambered() < weaponProperties.getChambers();

        if (!fineToFetch)
            return;

        timer = fetchTime.asFloat();

        onFetch(null);
        fetch();

        setState(State.fetching);
    }

    private boolean isPlayingCustomAnimation()
    {
        PlayerComponentData pc = data.getOwner().getComponent(PlayerComponentData.class);
        return pc != null && pc.isPlayingCustomAnimation();
    }

    public boolean doReload(boolean fineToAutoLoadRounds)
    {
        if (!isEnabled(false))
            return false;

        builtUp = false;

        if (weaponProperties.isUnlimited())
        {
            load();
            return true;
        }

        if (state == State.fetching ||
            state == State.loadMagazineRound ||
            state == State.reloading)
            return false;

        if (weaponProperties.isNoHalfReload() && getRounds() > 0)
        {
            return false;
        }

        if (data.getOwner() == null)
            return false;

        PlayerOwnerComponent owner = data.getOwner().getComponent(PlayerOwnerComponent.class);
        if (owner == null)
            return false;

        if (isPlayingCustomAnimation())
        {
            return false;
        }

        if (weaponProperties.canReloadBoth())
        {
            WeaponSlotComponent otherSlot;

            if (getSlot().equals(Constants.Properties.SLOT_PRIMARY))
            {
                otherSlot = getOtherSlot(Constants.Properties.SLOT_SECONDARY);
            }
            else
            {
                otherSlot = getOtherSlot(Constants.Properties.SLOT_PRIMARY);
            }

            if (otherSlot != null)
            {
                boolean canDoA = (
                    (owner.hasAmmo(getBullet()) && getRounds() < clipSize.asInt()
                    ) || isStuck()
                );

                boolean canDoB = (
                    (owner.hasAmmo(otherSlot.getBullet()) && otherSlot.getRounds() < otherSlot.clipSize.asInt()
                    ) || otherSlot.isStuck()
                );

                if (reloadBothTime.asFloat() > 0 && canDoA && canDoB)
                {
                    timer = reloadBothTime.asFloat();
                    onReload(Instrument.Action.reloadBoth);
                    loadBoth(otherSlot, otherSlot.getBullet());
                    setState(State.reloadingBoth);
                    return true;
                }

            }
        }

        if (data.getOwner() == null)
            return false;

        boolean fineToReload =
            reloadTime.asFloat() > 0 &&
            (
                (owner.hasAmmo(getBullet()) && getRounds() < clipSize.asInt()) || isStuck()
            );

        if (source.hasMagazineManagement())
        {
            fineToReload = false;

            IntMap.Keys keys = source.getMagazines();

            while (keys.hasNext)
            {
                int id = keys.next();
                if (source.getMagazineStatus(id) > 0)
                {
                    fineToReload = true;
                    break;
                }
            }
        }

        if (fineToReload)
        {
            timer = reloadTime.asFloat();

            onReload(null);
            load();

            setState(State.reloading);

            return true;
        }
        else
        {
            if (fineToAutoLoadRounds && source.hasMagazineManagement() && source.getMagazinesCount() > 0 && hasAmmo())
            {
                IntMap.Keys keys = source.getMagazines();

                if (keys.hasNext)
                {
                    int id = keys.next();
                    doLoadRounds(id);
                }

                return true;
            }
        }

        return false;
    }

    public float getCurrentAccuracy()
    {
        return currentAccuracy;
    }

    public float getAngleOffset()
    {
        return angleOffset;
    }

    private boolean applyWear()
    {
        if (launchesToStuck < 0)
        {
            // not initialized yet (server decides), so wait
            return true;
        }

        WeaponDurabilityComponentData wdcd = data.getComponent(WeaponDurabilityComponentData.class);

        if (wdcd != null)
        {
            if (wearResistance != null && wearResistance.asFloat() != 0)
            {
                float wear = 1.0f / wearResistance.asFloat();
                wdcd.decreaseDurability(wear);

                if (launchesToStuck > 0)
                {
                    launchesToStuck--;
                    return true;
                } else
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean testMisfire(int random)
    {
        if (getChamberedQuality() == -1)
            return false;

            /*
            float prob = 1.0f - Interpolation.exp5.apply((float)chamberedQuality / 100.0f);

            return MathUtils.random() < prob * 0.1f;
             */

        return MathUtils.randomBoolean();
    }

    private void updateAccuracy(float dt)
    {
        ActiveData activeData = data.getOwner();

        if (activeData instanceof PlayerData)
        {
            PlayerData playerData = ((PlayerData) activeData);

            float minValue = playerData.getState() == Player.State.sit ||
                playerData.getState() == Player.State.crawl ? 0.f :
                Constants.Weapons.ACCURACY_STAY_MIN_VALUE;

            currentAccuracy = MathUtils.clamp(currentAccuracy -
                    calculateBreakdown() * dt, minValue, 1f);
        }

        angleOffset *= 0.95f;
    }

    private boolean isStuck()
    {
        return state == State.stuck;
    }

    public ConsumableRecord getRecord()
    {
        return record;
    }

    public void loadBoth(WeaponSlotComponent otherSlot, Bullet otherBullet)
    {
        if (data.getOwner() != null &&
            (data.getOwner().getComponent(PlayerOwnerComponent.class).hasAmmo(otherBullet) || otherSlot.isStuck()))
        {
            loadRoundsBoth(otherSlot, otherBullet);
        }
    }

    public void load()
    {
        if (data.getOwner() == null)
            return;

        if ((source.hasMagazineManagement() && source.getMagazinesCount() > 0) ||
            data.getOwner().getComponent(PlayerOwnerComponent.class).hasAmmo(getBullet()) || isStuck())
        {
            loadRounds();
        }
    }

    public void fetch()
    {
        if (getRounds() > 0 && getChambered() < weaponProperties.getChambers())
        {
            fetchRounds();
        }
    }

    private void loadRounds()
    {
        onLoadMagazine();
    }

    private void fetchRounds()
    {
        onFetchRounds();
    }

    private void loadRoundsBoth(WeaponSlotComponent otherSlot, Bullet otherBullet)
    {
        onLoadRoundsBoth(otherSlot);
    }

    public void releaseRounds()
    {
        if (source.hasMagazineManagement())
        {
            detachMagazine();
            onUnloadMagazine();

            return;
        }


        if (bullet != null)
        {
            onUnloadMagazine();
            setRounds(0, -1);
        }
    }

    public void reload()
    {
        Instrument.Action action;

        if (getSlot().equals(Constants.Properties.SLOT_PRIMARY))
        {
            action = Instrument.Action.reload;
        }
        else
        {
            action = Instrument.Action.reloadSecondary;
        }

        BrainOut.EventMgr.sendDelayedEvent(data.getOwner(),
            InstrumentActionEvent.obtain(action, getReloadTime().asFloat(), getFetchTime().asFloat()));
    }

    protected WeaponSlotComponent getOtherSlot(String name)
    {
        return getOtherSlot.get(name);
    }

    public void pullRequired()
    {
        doFetch();
    }

    public void updateInfo(int rounds, int quality, int chambered, int chamberedQuality, boolean forceReset, int stuckIn)
    {
        if (rounds == -1)
        {
            source.detachMagazine(false);
        }
        else
        {
            if (isDetached())
            {
                attachMagazine(rounds, quality);
            }
            else
            {
                setRounds(rounds, quality);
            }
        }

        setChambered(chambered, chamberedQuality);
        this.launchesToStuck = stuckIn;
        if (forceReset)
        {
            this.forceReset = true;
        }
        BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.instrumentUpdated));
    }

    @Override
    public void init()
    {
    }

    public Weapon.WeaponProperties getWeaponProperties()
    {
        return weaponProperties;
    }

    public float getSpeedCoef()
    {
        return speedCoef.asFloat();
    }

    public WeaponData getData()
    {
        return data;
    }

    public int getShootSequence()
    {
        return shootSequence;
    }

    public UpgradableProperty getAimDistance()
    {
        return aimDistance;
    }

    public UpgradableProperty getReloadTime()
    {
        return reloadTime;
    }

    public UpgradableProperty getFetchTime()
    {
        return fetchTime;
    }

    public UpgradableProperty getAddMagazineRoundTime()
    {
        return addMagazineRoundTime;
    }

    public UpgradableProperty getCockTime()
    {
        return cockTime;
    }

    public UpgradableProperty getMagazinesCount()
    {
        return magazinesCount;
    }

    public String getSlot()
    {
        return slot;
    }

    public State getState()
    {
        return state;
    }

    protected abstract void onUpdateState();
    protected abstract void onBuildUp();
    protected abstract void onStuck();
    protected abstract void onUnloadMagazine();

    protected abstract boolean isBot();

    protected abstract void onReload(Instrument.Action action);
    protected abstract void onFetch(Instrument.Action action);
    protected abstract void onCock();
    protected abstract void onReset(boolean resetHandAnimations);
    protected abstract void onLoadRound(int magazine);
    protected abstract void onUnloadRounds(int magazine);
    protected abstract void onStopLoadRounds(int magazine);
    protected abstract void onLoadMagazine();
    protected abstract void onFetchRounds();
    protected abstract void onLoadRoundsBoth(WeaponSlotComponent otherSlot);
}
