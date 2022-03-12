package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.consumable.ConsumableContainer;

public class FreeplayInventoryUserPanel extends UserPanel
{
    private final ConsumableContainer inventory;

    public FreeplayInventoryUserPanel(ConsumableContainer inventory)
    {
        this.user = BrainOutClient.ClientController.getUserProfile();
        this.inventory = inventory;

        fill();
    }

    @Override
    protected void renderCustomStats()
    {
        ConsumableContent junk = BrainOutClient.ContentMgr.get("consumable-item-junk", ConsumableContent.class);
        int junkAmount = inventory.getAmount(junk);
        addStat(String.valueOf(junkAmount), "icon-junk", 0, 80);
    }
}
