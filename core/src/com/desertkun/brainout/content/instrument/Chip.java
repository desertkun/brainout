package com.desertkun.brainout.content.instrument;

import com.desertkun.brainout.data.instrument.ChipData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.Chip")
public class Chip extends Instrument
{
    @Override
    public ChipData getData(String dimension)
    {
        return new ChipData(this, dimension);
    }
}
