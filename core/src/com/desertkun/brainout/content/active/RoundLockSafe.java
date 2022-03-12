package com.desertkun.brainout.content.active;

import com.desertkun.brainout.data.active.RoundLockSafeData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.RoundLockSafe")
public class RoundLockSafe extends Item
{
    @Override
    public RoundLockSafeData getData(String dimension)
    {
        return new RoundLockSafeData(this, dimension);
    }
}
