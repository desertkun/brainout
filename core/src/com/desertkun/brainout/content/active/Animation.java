package com.desertkun.brainout.content.active;

import com.desertkun.brainout.data.active.AnimationData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Animation")
public class Animation extends Active
{
    @Override
    public AnimationData getData(String dimension)
    {
        return new AnimationData(this, dimension);
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }
}
