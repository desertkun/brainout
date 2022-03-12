package com.desertkun.brainout.data.instrument;

import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.instrument.ThrowableInstrument;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.ThrowableInstrumentData")
public class ThrowableInstrumentData extends InstrumentData
{
    public ThrowableInstrumentData(ThrowableInstrument instrument, String dimension)
    {
        super(instrument, dimension);
    }

    public ThrowableInstrument getInstrument()
    {
        return (ThrowableInstrument)super.getInstrument();
    }

    public ThrowableActive getThrowActive()
    {
        return ((ThrowableInstrument) getContent()).getThrowActive();
    }
}
