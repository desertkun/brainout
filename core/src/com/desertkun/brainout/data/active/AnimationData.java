package com.desertkun.brainout.data.active;

import com.desertkun.brainout.content.active.Active;

import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.AnimationData")
public class AnimationData extends PointData
{
    public AnimationData(Active active, String dimension)
    {
        super(active, dimension);
    }
}
