package com.desertkun.brainout.data.instrument;

import com.desertkun.brainout.content.instrument.Box;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.BoxData")
public class BoxData extends PlaceBlockData
{
    public BoxData(Box placeBlock, String dimension)
    {
        super(placeBlock, dimension);
    }
}
