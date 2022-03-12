package com.desertkun.brainout.content.components;


import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PartialInstrumentCardComponent")
public class PartialInstrumentCardComponent extends ContentCardComponent
{
    @Override
    public boolean applicable(UserProfile profile)
    {
        if (!super.applicable(profile))
        {
            return false;
        }

        InstrumentSlotItem instrumentSlot = ((InstrumentSlotItem) content);
        ContentLockTree.LockItem lockItem = instrumentSlot.getLockItem();

        if (lockItem == null)
        {
            return false;
        }

        int need = lockItem.getParam();

        Instrument instrument = instrumentSlot.getInstrument();
        return profile.getStats().get(instrument.getPartsStat(), 0.0f) < need;
    }

}
