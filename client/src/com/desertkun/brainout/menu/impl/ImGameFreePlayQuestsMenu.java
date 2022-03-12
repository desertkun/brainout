package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.L;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.MenuHelper;

public class ImGameFreePlayQuestsMenu extends FreePlayQuestsMenu
{
    private final PlayerData playerData;

    public ImGameFreePlayQuestsMenu(PlayerData playerData)
    {
        this.playerData = playerData;
    }

    @Override
    protected void addRightTopButtons()
    {
        MenuHelper.AddSingleButton(this, L.get("MENU_INVENTORY"), () ->
        {
            Menu.playSound(MenuSound.select);

            popMeAndPushMenu(new ExchangeInventoryMenu(playerData));
        });
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.TAB:
            {
                if (escape())
                {
                    return true;
                }
            }
        }

        return super.keyDown(keyCode);
    }

    @Override
    protected void renderQuestActionButtons(Table renderTo)
    {
        //
    }

    @Override
    protected int getQuestsPanelHeight()
    {
        return 512;
    }
}
