package com.desertkun.brainout.data.instrument;

import com.desertkun.brainout.content.instrument.Instrument;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.ActivateInstrumentData")
public class ActivateInstrumentData extends InstrumentData
{
    public ActivateInstrumentData(Instrument instrument, String dimension)
    {
        super(instrument, dimension);
    }
}
