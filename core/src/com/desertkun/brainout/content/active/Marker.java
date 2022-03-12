package com.desertkun.brainout.content.active;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.MarkerData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Marker")
public class Marker extends Active
{
    @Override
    public ActiveData getData(String dimension)
    {
        return new MarkerData(this, dimension);
    }
}
