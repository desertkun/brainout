package com.desertkun.brainout.content;


import com.desertkun.brainout.content.shop.PlayerSkinSlotItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.PlayerSkin")
public class PlayerSkin extends Skin
{
    private PlayerSkinSlotItem slotItem;

    public void setSlotItem(PlayerSkinSlotItem slotItem)
    {
        this.slotItem = slotItem;
    }

    public PlayerSkinSlotItem getSlotItem()
    {
        return slotItem;
    }
}
