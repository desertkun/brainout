package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.active.ShootingRange;
import com.desertkun.brainout.content.components.ClientShootingRangeActivatorComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.mode.ClientLobbyRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeLobby;
import com.desertkun.brainout.mode.GameModeRealization;
import com.desertkun.brainout.online.ClientEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import org.json.JSONObject;

@Reflect("ClientShootingRangeActivatorComponent")
@ReflectAlias("data.components.ClientShootingRangeActivatorComponentData")
public class ClientShootingRangeActivatorComponentData extends ClientActiveActivatorComponentData<ClientShootingRangeActivatorComponent>
{
    public ClientShootingRangeActivatorComponentData(
            ActiveData activeData,
            ClientShootingRangeActivatorComponent activatorComponent)
    {
        super(activeData, activatorComponent);

    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean test(PlayerData playerData)
    {
        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode instanceof GameModeLobby)
        {
            GameModeRealization realization = gameMode.getRealization();

            if (realization instanceof ClientLobbyRealization)
            {
                if (((ClientLobbyRealization) realization).isInShootingRangeMode())
                {
                    return false;
                }
            }
        }

        boolean hasWeapon = false;

        for (ClientEvent event : BrainOutClient.ClientController.getOnlineEvents())
        {
            if (!event.getEvent().isValid())
                continue;

            if (event.getEvent().taskAction.equals(Constants.Other.SHOOTING_RANGE_ACTION))
            {
                String data = event.getEvent().taskData;

                if (getShootingRange().hasWeapon(data))
                {
                    hasWeapon = true;
                    break;
                }
            }
        }

        if (!hasWeapon)
        {
            return false;
        }

        return super.test(playerData);
    }

    @Override
    public String getFailedConditionLocalizedText()
    {
        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode instanceof GameModeLobby)
        {
            GameModeRealization realization = gameMode.getRealization();

            if (realization instanceof ClientLobbyRealization)
            {
                if (((ClientLobbyRealization) realization).isInShootingRangeMode())
                {
                    return "";
                }
            }
        }
        return L.get("MENU_DOOR_LOCKED_UP");
    }

    public ShootingRange getShootingRange()
    {
        return (ShootingRange) getComponentObject().getContent();
    }

    @Override
    public boolean activate(PlayerData playerData)
    {
        if (!test(playerData))
            return false;

        startShooting();

        return true;
    }

    public void startShooting()
    {
        ShootingRange r = getShootingRange();

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");

        BrainOutClient.getInstance().topState().pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("key", r.getID());

        BrainOutClient.SocialController.sendRequest("start_shooting_range", args,
            new SocialController.RequestCallback()
            {
                @Override
                public void success(JSONObject response)
                {
                    waitLoadingMenu.pop();
                }

                @Override
                public void error(String reason)
                {
                    waitLoadingMenu.pop();
                }
            });
    }
}
