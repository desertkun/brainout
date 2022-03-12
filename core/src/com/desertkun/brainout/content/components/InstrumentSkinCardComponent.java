package com.desertkun.brainout.content.components;

import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.instrument.InstrumentSkin;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentSkinCardComponent")
public class InstrumentSkinCardComponent extends ContentCardComponent
{
    @Override
    public boolean applicable(UserProfile profile)
    {
        OwnableContent content = getOwnableContent();

        if (content instanceof InstrumentSkin)
        {
            InstrumentSkin skin = ((InstrumentSkin) content);
            InstrumentSlotItem slotItem = skin.getSlotItem();

            if (slotItem != null && !slotItem.hasItem(profile))
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        return super.applicable(profile);
    }
}
