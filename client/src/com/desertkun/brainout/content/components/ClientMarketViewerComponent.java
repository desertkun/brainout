package com.desertkun.brainout.content.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Achievement;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.MarketMenu;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientMarketViewerComponent")
public class ClientMarketViewerComponent extends ClientItemActivatorComponent
{
    @Override
    public void activate(ConsumableRecord record)
    {
        Achievement marketPass = BrainOut.ContentMgr.get("market-pass", Achievement.class);
        if (marketPass != null)
        {
            if (!BrainOutClient.ClientController.getUserProfile().hasItem(marketPass))
            {
                Menu.playSound(Menu.MenuSound.denied);
                return;
            }
        }

        BrainOutClient.getInstance().topState().pushMenu(new MarketMenu("default"));
    }
}
