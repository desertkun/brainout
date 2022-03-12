package com.desertkun.brainout.data.interfaces;

import com.desertkun.brainout.online.UserProfile;

public interface WithBadge
{
    public enum Involve
    {
        itemOnly,
        withChild,
        childOnly
    }

    boolean hasBadge(UserProfile profile, Involve involve);

    String getBadgeId();
}
