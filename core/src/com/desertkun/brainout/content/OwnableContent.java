package com.desertkun.brainout.content;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.shop.Shop;
import com.desertkun.brainout.data.interfaces.WithBadge;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.OwnableContent")
public class OwnableContent extends Content implements WithBadge
{
    public OwnableContent()
    {
    }

    public boolean hasItem(UserProfile userProfile)
    {
        return userProfile.hasItem(this);
    }

    public boolean hasItem(UserProfile userProfile, boolean lockCheck)
    {
        return userProfile.hasItem(this, lockCheck);
    }

    public boolean isLocked(UserProfile userProfile)
    {
        return ContentLockTree.getInstance() != null &&
                !ContentLockTree.getInstance().isItemUnlocked(this, userProfile);
    }

    public Shop.ShopItem getShopItem()
    {
        return Shop.getInstance().getItem(this);
    }

    public ContentLockTree.LockItem getLockItem()
    {
        if (ContentLockTree.getInstance() == null)
        {
            return null;
        }

        return ContentLockTree.getInstance().getItem(this);
    }

    public boolean isFree()
    {
        return Shop.getInstance() == null || Shop.getInstance().isFree(this);

    }

    public void addItem(UserProfile userProfile, int amount)
    {
        userProfile.addItem(this, amount);
    }

    @Override
    public boolean hasBadge(UserProfile profile, Involve involve)
    {
        return profile.hasBadge(getBadgeId());
    }

    @Override
    public String getBadgeId()
    {
        return getID();
    }
}
