package com.desertkun.brainout.content.instrument;

import com.desertkun.brainout.data.instrument.ActivateInstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.ActivateInstrument")
public class ActivateInstrument extends Instrument
{
    @Override
    public InstrumentData getData(String dimension)
    {
        return new ActivateInstrumentData(this, dimension);
    }
}
