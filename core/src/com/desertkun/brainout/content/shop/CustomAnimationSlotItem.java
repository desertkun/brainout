package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.ReplaceSlotComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.shop.CustomAnimationSlotItem")
public class CustomAnimationSlotItem extends ConsumableSlotItem
{
    private CustomAnimationSlotSelection selection;

    public class CustomAnimationSlotSelection extends ConsumableSelection
    {
        public CustomAnimationSlotItem getItem()
        {
            return ((CustomAnimationSlotItem) super.getItem());
        }

        @Override
        public void apply(ShopCart shopCart, PlayerData playerData, UserProfile profile, Slot slot, Selection selection)
        {
            ReplaceSlotComponent replaceSlotComponent = getComponent(ReplaceSlotComponent.class);

            if (replaceSlotComponent != null)
            {
                for (ObjectMap.Entry<String, String> entry : replaceSlotComponent.getReplace())
                {
                    playerData.setCustomAnimationSlot(entry.key, entry.value);
                }
            }

            super.apply(shopCart, playerData, profile, slot, selection);
        }
    }

    @Override
    public Selection getSelection()
    {
        if (selection == null)
        {
            selection = new CustomAnimationSlotSelection();
        }

        return selection;
    }
}
