package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.RandomWeightComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.quest.DailyQuest;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public abstract class TakeOutRandomItem extends Task
{
    private static Array<ConsumableContent> weaponPool;
    private static Array<ConsumableContent> ammoPool;
    private static Array<ConsumableContent> otherPool;
    private static Array<ConsumableContent> dogTagsPool;

    public enum Category
    {
        weapon,
        ammo,
        other,
        dogtags,
    }

    private Category category;
    private String hash;
    private int share;

    private Array<ConsumableContent> cachedPool;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        category = Category.valueOf(jsonData.getString("category", Category.other.toString()));
        hash = jsonData.getString("hash", "");
        share = jsonData.getInt("share", 0);
    }

    private Array<ConsumableContent> getWeaponPool()
    {
        if (weaponPool != null)
            return weaponPool;

        weaponPool = BrainOut.ContentMgr.queryContent(Weapon.class,
            c ->
        {
            Weapon weapon = ((Weapon) c);

            InstrumentSlotItem slot = weapon.getSlotItem();

            if (slot == null)
                return false;

            if (RandomWeightComponent.Get(slot) == 0)
                return false;

            if (slot.getDefaultSkin() == null)
                return false;

            if (weapon.getPrimaryProperties().isUnlimited())
                return false;

            if (!slot.getSlot().getID().equals("slot-primary"))
                return false;

            return true;
        });

        weaponPool.sort(Comparator.comparing(Content::getID));

        return weaponPool;
    }

    private Array<ConsumableContent> getAmmoPool()
    {
        if (ammoPool != null)
            return ammoPool;

        ammoPool = BrainOut.ContentMgr.queryContent(Bullet.class,
            bullet -> RandomWeightComponent.Get(bullet) != 0);

        ammoPool.sort(Comparator.comparing(Content::getID));

        return ammoPool;
    }

    private Array<ConsumableContent> getDogTagsPool()
    {
        if (dogTagsPool != null)
            return dogTagsPool;

        dogTagsPool = BrainOut.ContentMgr.queryContent(ConsumableContent.class,
            tag -> "freeplay-dog-tags".equals(tag.getID()));

        return dogTagsPool;
    }

    private Array<ConsumableContent> getOtherPool()
    {
        if (otherPool != null)
            return otherPool;

        otherPool = BrainOut.ContentMgr.queryContent(ConsumableContent.class,
            consumable ->
        {
            if (consumable instanceof Weapon)
                return false;

            if (consumable instanceof Bullet)
                return false;

            if (RandomWeightComponent.Get(consumable) == 0)
                return false;

            return consumable.getClass() == ConsumableContent.class;
        });

        otherPool.sort(Comparator.comparing(Content::getID));

        return otherPool;
    }

    private Array<ConsumableContent> getPool()
    {
        switch (category)
        {
            case weapon:
                return getWeaponPool();
            case ammo:
                return getAmmoPool();
            case dogtags:
                return getDogTagsPool();
            case other:
            default:
                return getOtherPool();
        }
    }

    public ConsumableContent getItem(String account)
    {
        if (cachedPool == null)
        {
            cachedPool = getPool();

            if (cachedPool.size == 0)
                return null;

            cachedPool.sort((o1, o2) ->
            {
                RandomWeightComponent w1 = o1.getComponent(RandomWeightComponent.class);
                RandomWeightComponent w2 = o2.getComponent(RandomWeightComponent.class);

                if (w1 == null || w2 == null)
                {
                    return 0;
                }

                return w2.getWeight() - w1.getWeight();
            });
        }

        return RandomWeightComponent.GetRandomItem(cachedPool, getDateStableRandom(account));
    }

    @Override
    public int getTarget(String account)
    {
        if (!BrainOut.OnlineEnabled())
        {
            return 1;
        }

        switch (category)
        {
            case weapon:
                return 1;

            case dogtags:
                return 3;

            case ammo:
            {
                ConsumableContent ammo = getItem(account);
                if (ammo instanceof Bullet)
                {
                    return ((Bullet) ammo).getGood() * 2;
                }
                else
                {
                    return 10;
                }
            }
            case other:
            default:
            {
                return 1 + getDateStableRandom(account) % 2;
            }
        }
    }

    protected abstract long getCurrentTime();

    private long getCurrentDay()
    {
        return getCurrentTime() / ((DailyQuest) getQuest()).getCycle();
    }

    protected int getDateStableRandom(String account)
    {
        if (!BrainOut.OnlineEnabled())
        {
            return 0;
        }

        MessageDigest digest;

        try
        {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return 0;
        }

        String payload = String.valueOf(getCurrentDay()) + category + hash;

        if (share > 1)
        {
            int a;

            try
            {
                a = Integer.parseInt(account);
            }
            catch (NumberFormatException igored)
            {
                a = -1;
            }

            if (a > 0)
            {
                payload += String.valueOf(a % share);
            }
        }

        byte[] hash = digest.digest(payload.getBytes());
        return Math.abs(new BigInteger(hash).intValue());
    }

    public Category getCategory()
    {
        return category;
    }
}
