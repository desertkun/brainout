package com.desertkun.brainout.content.upgrades;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.shop.Shop;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.upgrades.UpgradeChain")
public class UpgradeChain extends OwnableContent
{
    private Array<Upgrade> upgrades;
    private String levels;
    private String prefix;
    private boolean enabled;

    public class ChainedUpgrade extends Upgrade
    {
        private final Upgrade original;
        private final int level;

        public ChainedUpgrade(int level, Upgrade original)
        {
            this.level = level;
            this.original = original;
            this.properties = original.getProperties();
            this.name = original.getTitle();
            this.description = original.getDescription();
        }

        @Override
        public Array<ContentComponent> getComponents()
        {
            return original.getComponents();
        }

        @Override
        public <T extends ContentComponent> T getComponent(Class<T> classOf)
        {
            return original.getComponent(classOf);
        }

        @Override
        public <T extends ContentComponent> T getComponentFrom(Class<T> classOf)
        {
            return original.getComponentFrom(classOf);
        }

        @Override
        public boolean hasComponent(Class<? extends ContentComponent> clazz)
        {
            return original.hasComponent(clazz);
        }

        public int getMyLevel(UserProfile userProfile)
        {
            return userProfile.getLevel(levels);
        }

        public int getItemLevel(UserProfile userProfile)
        {
            return userProfile.itemsHave(UpgradeChain.this);
        }

        @Override
        public boolean isFree()
        {
            return original.isFree();
        }

        public int getLevel()
        {
            return level;
        }

        @Override
        public boolean hasItem(UserProfile userProfile)
        {
            int itemLevel = getItemLevel(userProfile);
            return itemLevel >= level;
        }

        @Override
        public void addItem(UserProfile userProfile, int amount)
        {
            UpgradeChain.this.addItem(userProfile, amount);
        }

        @Override
        public Shop.ShopItem getShopItem()
        {
            return original.getShopItem();
        }

        @Override
        public ContentLockTree.LockItem getLockItem()
        {
            return original.getLockItem();
        }

        @Override
        public boolean isLocked(UserProfile userProfile)
        {
            return original.isLocked(userProfile);
        }
    }

    public UpgradeChain()
    {
        upgrades = new Array<>();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        levels = jsonData.getString("level-kind");
        prefix = jsonData.getString("prefix");
        enabled = jsonData.getBoolean("enabled", true);

        if (jsonData.has("upgrades"))
        {
            int level = 1;

            for (JsonValue value : jsonData.get("upgrades"))
            {
                Upgrade original = ((Upgrade) BrainOut.ContentMgr.get(value.asString()));

                if (original == null)
                {
                    throw new RuntimeException("Incorrect original reference.");
                }

                String id = prefix + "-" + original.getID();

                Upgrade upgrade = new ChainedUpgrade(level++, original);
                upgrade.setID(id);
                upgrade.setPackage(getPackage());

                BrainOut.ContentMgr.registerItem(id, upgrade);

                upgrades.add(upgrade);
            }
        }
    }

    public int getUpgradeLevel(UserProfile profile)
    {
        return MathUtils.clamp(profile.itemsHave(this), 0, upgrades.size - 1);
    }

    public Upgrade getUpgrade(UserProfile profile)
    {
        int level = getUpgradeLevel(profile);

        if (level == 0)
        {
            return null;
        }

        return upgrades.get(level - 1);
    }

    public Array<Upgrade> getUpgrades()
    {
        return upgrades;
    }
}
