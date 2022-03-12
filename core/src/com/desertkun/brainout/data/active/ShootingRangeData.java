package com.desertkun.brainout.data.active;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.interfaces.WithTag;

public class ShootingRangeData extends PointData
{
    public ShootingRangeData(Active active, String dimension)
    {
        super(active, dimension);
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.SHOOTING_RANGE);
    }
}
