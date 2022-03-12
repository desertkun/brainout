package com.desertkun.brainout.data.active;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.ThrowableActiveData")
public class ThrowableActiveData extends PointData implements WithTag
{
    private InstrumentInfo launchedBy;
    private float power;

    public ThrowableActiveData(Active active, String dimension)
    {
        super(active, dimension);
    }

    public InstrumentInfo getLaunchedBy()
    {
        return launchedBy;
    }

    public void setLaunchedBy(InstrumentInfo launchedBy)
    {
        this.launchedBy = launchedBy;
    }

    public void setPower(float power)
    {
        this.power = power;
    }

    public float getPower()
    {
        return power;
    }

    @Override
    public int getTags()
    {
        return super.getTags() | WithTag.TAG(Constants.ActiveTags.THROWABLE);
    }
}
