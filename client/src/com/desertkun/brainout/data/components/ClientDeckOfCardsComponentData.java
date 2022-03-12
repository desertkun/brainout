package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.ClientDeckOfCardsComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.menu.impl.FreePlayCardsGameMenu;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientDeckOfCardsComponent")
@ReflectAlias("data.components.ClientDeckOfCardsComponentData")
public class ClientDeckOfCardsComponentData extends ClientActiveActivatorComponentData<ClientDeckOfCardsComponent>
{
    public ClientDeckOfCardsComponentData(ActiveData activeData, ClientDeckOfCardsComponent activatorComponent)
    {
        super(activeData, activatorComponent);
    }

    @Override
    public boolean activate(PlayerData playerData)
    {
        if (BrainOutClient.getInstance().topState().topMenu() instanceof FreePlayCardsGameMenu)
            return true;

        BrainOutClient.getInstance().topState().pushMenu(new FreePlayCardsGameMenu(getActiveData(), this));
        return true;
    }
}
