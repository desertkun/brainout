package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.components.DurabilityComponent;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.components.SecondaryWeaponSlotComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.components.SecondaryWeaponSlotComponentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

import java.util.Objects;

public class InstrumentCharacteristics extends CharacteristicsPanel
{
    private final InstrumentInfo info;
    private HoverUpgrade hover;
    private int quality;

    private class HoverUpgrade
    {
        public final String upgradeClass;
        public final Upgrade upgrade;

        public HoverUpgrade(String upgradeClass, Upgrade upgrade)
        {
            this.upgradeClass = upgradeClass;
            this.upgrade = upgrade;
        }
    }

    public InstrumentCharacteristics(InstrumentInfo info, int quality)
    {
        this.info = info;
        this.hover = null;
        this.quality = quality;

        initChars();
    }

    public void clearHoverUpgrade()
    {
        this.hover = null;
    }

    public void setHoverUpgrade(String upgradeClass, Upgrade upgrade)
    {
        this.hover = new HoverUpgrade(upgradeClass, upgrade);
    }

    public float getProperty(InstrumentInfo info, String key)
    {
        return getProperty(info, key, 0);
    }

    public String getPropertyString(InstrumentInfo info, String key, String def)
    {
        Instrument.InstrumentProperties properties = info.instrument.getPrimaryProperties();

        UpgradableProperty property = properties.property(key);

        if (property != null)
        {
            String value = property.asString();

            for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
            {
                if (hover != null && entry.key.equals(hover.upgradeClass))
                {
                    // don't apply this class of upgrades, because hover upgrade applies later
                    continue;
                }

                Upgrade upgrade = entry.value;

                if (upgrade != null)
                {
                    Upgrade.UpgradeProperty upd = upgrade.getProperty(key);
                    if (upd != null)
                    {
                        value = upd.apply(value);
                    }
                }
            }

            if (hover != null)
            {
                Upgrade.UpgradeProperty upd = hover.upgrade.getProperty(key);
                if (upd != null)
                {
                    value = upd.apply(value);
                }
            }

            return value;
        }

        return def;
    }

    public float getProperty(InstrumentInfo info, String key, float def)
    {
        Instrument.InstrumentProperties properties = info.instrument.getPrimaryProperties();

        UpgradableProperty property = properties.property(key);

        if (property != null)
        {
            String value = property.asString();

            for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
            {
                if (hover != null && entry.key.equals(hover.upgradeClass))
                {
                    // don't apply this class of upgrades, because hover upgrade applies later
                    continue;
                }

                Upgrade upgrade = entry.value;

                if (upgrade != null)
                {
                    Upgrade.UpgradeProperty upd = upgrade.getProperty(key);
                    if (upd != null)
                    {
                        value = upd.apply(value);
                    }
                }
            }

            if (hover != null)
            {
                Upgrade.UpgradeProperty upd = hover.upgrade.getProperty(key);
                if (upd != null)
                {
                    value = upd.apply(value);
                }
            }

            return Float.valueOf(value);
        }

        return 0;
    }

    private class BulletView extends View
    {
        @Override
        public void render(Data from, Table to)
        {
            String bulletName = getPropertyString(info, Constants.Properties.BULLET, null);
            if (bulletName == null)
                return;

            Bullet bullet = BrainOutClient.ContentMgr.get(bulletName, Bullet.class);
            if (bullet == null)
                return;

            String suffix = bullet.getTitle().get();

            IconComponent iconComponent = bullet.getComponent(IconComponent.class);

            if (iconComponent == null)
                return;

            String icon = iconComponent.getIconName("icon", null);
            Label valueLabel = new Label(L.get(suffix), BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            if (icon != null)
            {
                TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(icon);

                if (region != null)
                {
                    Image image = new Image(region);
                    image.setScaling(Scaling.fit);
                    to.add(image).maxHeight(16).expandX().right().padRight(-8);
                }
            }

            to.add(valueLabel).right();
        }
    }

    private void initChars()
    {
        if ((info.instrument instanceof Weapon) && !((Weapon) info.instrument).isNoWeaponStats())
        {
            Weapon weapon = ((Weapon) info.instrument);

            Weapon.WeaponProperties properties = weapon.getPrimaryProperties();

            add("char-accuracy", "CHAR_ACCURACY",
                () -> getProperty(info, Constants.Properties.ACCURACY),
                new CharacteristicsPanel.ProgressView(0, 100));

            add("char-recoil", "CHAR_RECOIL",
                () -> getProperty(info, Constants.Properties.RECOIL),
                new CharacteristicsPanel.ProgressView(0, 100));

            if (properties.getBullet() != null)
            {
                add("char-damage", "CHAR_DAMAGE", () -> getProperty(info, Constants.Properties.DAMAGE),
                    new CharacteristicsPanel.ProgressView(0, 100));
            }

            if (!properties.isPullRequired() &&
                !properties.getShootModes().contains(Weapon.ShootMode.singleCock, true))
            {
                add("char-fire-rate", "CHAR_FIRE_RATE", () ->
                        getProperty(info, Constants.Properties.FIRE_RATE) * weapon.getPrimaryProperties().getBulletAtLaunch(),
                        new CharacteristicsPanel.SimpleView("CHAR_SUFFIX_RATE"));
            }

            if (getProperty(info, Constants.Properties.RELOAD_TIME) > 0.2)
            {
                add("char-reload-speed", "CHAR_RELOAD_SPEED", () -> getProperty(info, Constants.Properties.RELOAD_TIME),
                        new CharacteristicsPanel.FloatView("CHAR_SUFFIX_SEC"));
            }
        }

        ItemComponent item = info.instrument.getComponent(ItemComponent.class);

        if (item != null && item.getWeight() != 0)
        {
            add("char-weight", "CHAR_WEIGHT", item::getWeight,
                new CharacteristicsPanel.SimpleView("CHAR_SUFFIX_KG"));
        }

        if (info.instrument instanceof Weapon)
        {
            add("char-clip-size", "CHAR_CLIP_SIZE", () -> getProperty(info, Constants.Properties.CLIP_SIZE),
                new CharacteristicsPanel.SimpleView());

            if (info.instrument.getPrimaryProperties() instanceof Weapon.WeaponProperties)
            {
                Weapon.WeaponProperties weaponProperties =
                    ((Weapon.WeaponProperties) info.instrument.getPrimaryProperties());

                Bullet bullet = BrainOutClient.ContentMgr.get(weaponProperties.getBullet(), Bullet.class);

                if (bullet != null)
                {
                    IconComponent iconComponent = bullet.getComponent(IconComponent.class);

                    if (iconComponent != null)
                    {
                        add("char-cartridge", "CHAR_CARTRIDGE", () -> 0, new BulletView());
                    }
                }
            }

            SecondaryWeaponSlotComponent sws = info.instrument.getComponent(SecondaryWeaponSlotComponent.class);

            if (sws != null)
            {
                Weapon.WeaponProperties weaponProperties = sws.getWeaponProperties();

                Bullet bullet = BrainOutClient.ContentMgr.get(weaponProperties.getBullet(), Bullet.class);

                if (bullet != null)
                {
                    IconComponent iconComponent = bullet.getComponent(IconComponent.class);

                    if (iconComponent != null)
                    {
                        add(null, null, () -> 0,
                                new IconOnlyView(bullet.getTitle().get(),
                                        iconComponent.getIconName("icon", null)));
                    }
                }
            }
            else
            {
                for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
                {
                    SecondaryWeaponSlotComponent swsc = entry.value.getComponent(SecondaryWeaponSlotComponent.class);

                    if (swsc != null)
                    {
                        Weapon.WeaponProperties weaponProperties = swsc.getWeaponProperties();

                        Bullet bullet = BrainOutClient.ContentMgr.get(weaponProperties.getBullet(), Bullet.class);

                        if (bullet != null)
                        {
                            IconComponent iconComponent = bullet.getComponent(IconComponent.class);

                            if (iconComponent != null)
                            {
                                add(null, null, () -> 0,
                                        new IconOnlyView(bullet.getTitle().get(),
                                                iconComponent.getIconName("icon", null)));
                            }
                        }

                        break;
                    }
                }
            }

        }

        if (!BrainOutClient.ClientController.isFreePlay())
        {
            DurabilityComponent dc = info.instrument.getComponentFrom(DurabilityComponent.class);

            if (dc != null)
            {
                add("char-wear-resistance", "CHAR_WEAR_RESISTANCE",
                        () -> getProperty(info, Constants.Properties.WEAR_RESISTANCE),
                        new CharacteristicsPanel.SimpleIconView("CHAR_SUFFIX_ROUNDS", "char-durability"));
            }

            if (dc != null)
            {
                add("char-durability", "CHAR_DURABILITY",
                        () -> (float)Math.ceil(dc.getDurability(BrainOutClient.ClientController.getUserProfile())),
                        new CharacteristicsPanel.NofNView(dc.getDurability()));
            }
        }

        if (quality != -1)
        {
            add("char-durability", "CHAR_DURABILITY", () -> quality, new CharacteristicsPanel.SimpleView("%"));
        }
    }
}
