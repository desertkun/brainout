package com.desertkun.brainout.content.components.interfaces;

import com.desertkun.brainout.data.instrument.InstrumentData;

public interface UpgradeComponent
{
    void upgrade(InstrumentData instrumentData);
    boolean pre();
}
