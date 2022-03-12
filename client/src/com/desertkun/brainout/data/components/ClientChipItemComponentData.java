package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ClientChipItemComponent;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.WithTag;

import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientChipItemComponent")
@ReflectAlias("data.components.ClientChipItemComponentData")
public class ClientChipItemComponentData extends ClientItemComponentData implements WithTag
{
    public ClientChipItemComponentData(ItemData itemData,
        ClientChipItemComponent clientItemComponent)
    {
        super(itemData, clientItemComponent);
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.CHIP);
    }

    @Override
    protected boolean canTake(PlayerData playerData)
    {
        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode != null)
        {
            if (!gameMode.isGameActive(true, true))
            {
                return false;
            }
        }

        return super.canTake(playerData);
    }
}
