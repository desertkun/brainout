package com.desertkun.brainout.content.instrument;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.WeaponDurabilityComponent;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.data.instrument.WeaponData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.Weapon")
public class Weapon extends Instrument
{
    public static class WeaponProperties extends InstrumentProperties
    {
        private float accuracy;
        private float recoil;

        private int clipSize;
        private float reloadTime;
        private float fetchTime;
        private float reloadBothTime;
        private float cockTime;
        private boolean unlimited;
        private Array<ShootMode> shootModes;
        private String bullet;
        private boolean silenced;
        private boolean perBulletReload;
        private boolean pullRequired;
        private boolean noHalfReload;
        private boolean noUnload;
        private boolean autoLoad;
        private boolean autoReload;
        private boolean shellAutoEject;
        private boolean hasBuildUp;
        private float buildUpTime;
        private float damage;
        private int aimDist;
        private int bulletAtLaunch;
        private int magazineManagement;
        private float magazineAddRoundTime;
        private boolean visible;
        private boolean autoSwitchSecondary;
        private boolean canReloadBoth;
        private float fireRate;
        private float burst2FireRate;
        private int chambers;

        public WeaponProperties()
        {
            bullet = null;
            shootModes = new Array<>();
            accuracy = 25;
            recoil = 75;
            silenced = false;
            aimDist = 0;
            damage = 10.0f;
            bulletAtLaunch = 1;
            chambers = 1;
            hasBuildUp = false;
            magazineManagement = 0;
            magazineAddRoundTime = 0.3f;
            buildUpTime = 0;
            noHalfReload = false;
            noUnload = false;
            visible = true;
            shellAutoEject = true;
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            accuracy = jsonData.getFloat("accuracy", accuracy);
            recoil = jsonData.getFloat("recoil", recoil);
            shellAutoEject = jsonData.getBoolean("shell-auto-eject", shellAutoEject);

            autoSwitchSecondary = jsonData.getBoolean("autoSwitchSecondary", false);
            canReloadBoth = jsonData.getBoolean("canReloadBoth", false);
            autoReload = jsonData.getBoolean("autoReload", true);

            if (jsonData.has("silenced"))
            {
                silenced = jsonData.getBoolean("silenced");
            }

            fireRate = jsonData.getFloat("fire-rate", 1);
            burst2FireRate = jsonData.getFloat("burst2-rate", fireRate);

            aimDist = jsonData.getInt("aimDistance", 0);
            clipSize = jsonData.getInt("clipSize", 1);
            reloadTime = jsonData.getFloat("reloadTime", 1);
            fetchTime = jsonData.getFloat("fetchTime", 0);
            buildUpTime = jsonData.getFloat("buildUpTime", 0);
            reloadBothTime = jsonData.getFloat("reloadBothTime", reloadTime);
            cockTime = jsonData.getFloat("cockTime", 0);
            unlimited = jsonData.getBoolean("unlimited", false);
            noHalfReload = jsonData.getBoolean("noHalfReload", false);
            noUnload = jsonData.getBoolean("noUnload", false);
            autoLoad = jsonData.getBoolean("autoLoad", false);
            hasBuildUp = jsonData.getBoolean("hasBuildUp", false);
            magazineManagement = jsonData.getInt("magazineManagement", 1) - 1;
            visible = jsonData.getBoolean("visible", visible);
            bulletAtLaunch = jsonData.getInt("bulletAtLaunch", 1);

            if (jsonData.has("shootModes"))
            {
                JsonValue s = jsonData.get("shootModes");

                if (s.isArray())
                {
                    for (JsonValue sm: s)
                    {
                        ShootMode shootMode = ShootMode.valueOf(sm.asString());
                        shootModes.add(shootMode);
                    }
                }
                else if (s.isString())
                {
                    ShootMode shootMode = ShootMode.valueOf(s.asString());
                    shootModes.add(shootMode);
                }
            }
            else
            {
                shootModes.add(ShootMode.single);
            }

            perBulletReload = jsonData.getBoolean("perBulletReload", false);
            pullRequired = jsonData.getBoolean("pullRequired", false);
            damage = jsonData.getFloat("damage", 10f);
            chambers = jsonData.getInt("chambers", 1);

            JsonValue allowedBullets = jsonData.get("allowedBullets");
            if (allowedBullets != null)
            {
                if (allowedBullets.isArray())
                {
                    String[] arr = allowedBullets.asStringArray();
                    if (arr.length > 0)
                    {
                        bullet = arr[0];
                    }
                }
                else
                {
                    bullet = allowedBullets.asString();
                }
            }
        }

        public String getBullet()
        {
            return bullet;
        }

        @Override
        public void initProperties()
        {
            addProperty(new UpgradableProperty(Constants.Properties.DAMAGE, getDamage()));
            addProperty(new UpgradableProperty(Constants.Properties.BULLET, getBullet()));
            addProperty(new UpgradableProperty(Constants.Properties.FIRE_RATE, getFireRate()));
            addProperty(new UpgradableProperty(Constants.Properties.FIRE_RATE_B2, getBurst2FireRate()));
            addProperty(new UpgradableProperty(Constants.Properties.ACCURACY, getAccuracy()));
            addProperty(new UpgradableProperty(Constants.Properties.RECOIL, getRecoil()));
            addProperty(new UpgradableProperty(Constants.Properties.RELOAD_TIME, getReloadTime()));
            addProperty(new UpgradableProperty(Constants.Properties.FETCH_TIME, getFetchTime()));
            addProperty(new UpgradableProperty(Constants.Properties.RELOAD_TIME_BOTH, getReloadBothTime()));
            addProperty(new UpgradableProperty(Constants.Properties.COCK_TIME, getCockTime()));
            addProperty(new UpgradableProperty(Constants.Properties.CLIP_SIZE, getClipSize()));
            addProperty(new UpgradableProperty(Constants.Properties.AIM_MARKER, "standard"));
            addProperty(new UpgradableProperty(Constants.Properties.SILENT, isSilenced() ? 1 : 0));
            addProperty(new UpgradableProperty(Constants.Properties.AIM_DISTANCE, getAimDist()));
            addProperty(new UpgradableProperty(Constants.Properties.SPEED_COEF, 1.0f));
            addProperty(new UpgradableProperty(Constants.Properties.SHOOT_MODES, getShootModes()));
            addProperty(new UpgradableProperty(Constants.Properties.NUMBER_OF_MAGAZINES, getStartingMagazinesCount()));
            addProperty(new UpgradableProperty(Constants.Properties.MAG_ADD_ROUND_TIME, getMagazineAddRoundTime()));
        }

        public int getChambers()
        {
            return chambers;
        }

        public boolean hasChambering()
        {
            return fetchTime > 0;
        }

        public int getBulletAtLaunch()
        {
            return bulletAtLaunch;
        }

        public boolean hasShellAutoEject()
        {
            return shellAutoEject;
        }

        public boolean isAutoSwitchSecondary()
        {
            return autoSwitchSecondary;
        }

        public boolean canReloadBoth()
        {
            return canReloadBoth;
        }

        public boolean isAutoReload()
        {
            return autoReload;
        }

        public boolean hasBuildUp()
        {
            return hasBuildUp;
        }

        public float getBuildUpTime()
        {
            return buildUpTime;
        }

        public boolean isVisible()
        {
            return visible;
        }

        public boolean hasMagazineManagement()
        {
            return magazineManagement != 0;
        }

        public int getStartingMagazinesCount()
        {
            return magazineManagement;
        }

        public float getMagazineAddRoundTime()
        {
            return magazineAddRoundTime;
        }

        public int getAimDist()
        {
            return aimDist;
        }

        public Array<ShootMode> getSmootModes()
        {
            return shootModes;
        }

        public float getDamage()
        {
            return damage;
        }

        public float getAccuracy()
        {
            return accuracy;
        }

        public float getRecoil()
        {
            return recoil;
        }

        public int getClipSize()
        {
            return clipSize;
        }

        public float getReloadTime()
        {
            return reloadTime;
        }

        public float getFetchTime()
        {
            return fetchTime;
        }

        public float getReloadBothTime()
        {
            return reloadBothTime;
        }

        public float getCockTime()
        {
            return cockTime;
        }

        public float getFireRate()
        {
            return fireRate;
        }

        public float getBurst2FireRate()
        {
            return burst2FireRate;
        }

        public boolean canUnload()
        {
            return !noUnload;
        }

        public boolean isAutoLoad()
        {
            return autoLoad;
        }

        public Array<ShootMode> getShootModes()
        {
            return shootModes;
        }

        public boolean isSilenced()
        {
            return silenced;
        }

        public boolean isUnlimited()
        {
            return unlimited;
        }

        public boolean isPullRequired()
        {
            return pullRequired;
        }

        public boolean isPerBulletReload()
        {
            return perBulletReload;
        }

        public boolean isNoHalfReload()
        {
            return noHalfReload;
        }
    }

    private Array<Skill> skills;
    private boolean noWeaponStats;

    public class Skill implements Json.Serializable
    {
        private int kills;

        public Skill() {}
        public Skill(int kills)
        {
            this.kills = kills;
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.kills = jsonData.getInt("kills");
        }

        public int getKills()
        {
            return kills;
        }
    }

    public enum ShootMode
    {
        single,
        singleCock,
        auto,
        burst,
        burst2,
        couple
    }

    public Weapon()
    {
        skills = new Array<>();
    }

    public WeaponData getWeaponData(String dimension)
    {
        return getData(dimension);
    }

    @Override
    protected void initProperties()
    {
        super.initProperties();

        WeaponDurabilityComponent wdc = getComponent(WeaponDurabilityComponent.class);

        if (wdc != null)
        {
            getPrimaryProperties().
                addProperty(new UpgradableProperty(Constants.Properties.WEAR_RESISTANCE, wdc.getWear()));
        }
    }

    @Override
    protected InstrumentProperties newProperties()
    {
        return new WeaponProperties();
    }

    @Override
    public WeaponProperties getPrimaryProperties()
    {
        return (WeaponProperties)super.getPrimaryProperties();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        noWeaponStats = jsonData.getBoolean("noWeaponStats", false);

        if (jsonData.has("skills"))
        {
            for (JsonValue value : jsonData.get("skills"))
            {
                Skill skill = new Skill();
                skill.read(json, value);
                this.skills.add(skill);
            }
        }
        else
        {
            skills.add(new Skill(25));
            skills.add(new Skill(50));
            skills.add(new Skill(100));
        }
    }

    @Override
    public WeaponData getData(String dimension)
    {
        return new WeaponData(this, dimension);
    }

    public Array<Skill> getSkills()
    {
        return skills;
    }

    public boolean isNoWeaponStats()
    {
        return noWeaponStats;
    }

    @Override
    protected boolean needLocalizationCheck()
    {
        return false;
    }
}
