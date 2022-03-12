package com.desertkun.brainout.data.instrument;

import com.desertkun.brainout.content.instrument.PlaceBlock;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.PlaceBlockData")
public class PlaceBlockData extends InstrumentData
{
    private final PlaceBlock placeBlock;

    public PlaceBlockData(PlaceBlock placeBlock, String dimension)
    {
        super(placeBlock, dimension);

        this.placeBlock = placeBlock;
    }

    public PlaceBlock getPlaceBlock()
    {
        return placeBlock;
    }
}
