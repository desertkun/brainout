package com.desertkun.brainout.utils;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.consumable.Walkietalkie;
import com.desertkun.brainout.content.consumable.impl.WalkietalkieConsumableItem;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class FPUtils
{
    public static boolean isPlayersHasWalkietalkieContact(PlayerData firstPlayer, PlayerData secondPlayer)
    {
        PlayerOwnerComponent myPoc = firstPlayer.getComponent(PlayerOwnerComponent.class);
        PlayerOwnerComponent otherPoc = secondPlayer.getComponent(PlayerOwnerComponent.class);

        if (myPoc != null && otherPoc != null)
        {
            Walkietalkie walkietalkie = BrainOutServer.ContentMgr.get("consumable-item-walkietalkie", Walkietalkie.class);

            ConsumableRecord myRecord = myPoc.getConsumableContainer().getConsumable(walkietalkie);

            if (myRecord != null)
            {
                ConsumableRecord otherRecord = otherPoc.getConsumableContainer().getConsumable(walkietalkie);

                if (otherRecord != null)
                {
                    WalkietalkieConsumableItem myWT = (WalkietalkieConsumableItem) myRecord.getItem();
                    WalkietalkieConsumableItem otherWT = (WalkietalkieConsumableItem) otherRecord.getItem();

                    if (myWT.getFrequency() == otherWT.getFrequency())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
