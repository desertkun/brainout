package com.desertkun.brainout.data.instrument;

import com.desertkun.brainout.content.instrument.Grenade;
import com.desertkun.brainout.content.instrument.ThrowableInstrument;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.Grenade")
public class GrenadeData extends ThrowableInstrumentData
{
    public GrenadeData(Grenade grenade, String dimension)
    {
        super(grenade, dimension);
    }
}
