package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.CSQuickPlay;
import com.desertkun.brainout.content.Achievement;
import com.desertkun.brainout.content.components.ClientMenuActivatorComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.GameService;

import java.util.Objects;

@Reflect("ClientMenuActivatorComponent")
@ReflectAlias("data.components.ClientMenuActivatorComponentData")
public class ClientMenuActivatorComponentData extends ClientActiveActivatorComponentData<ClientMenuActivatorComponent>
{
    private RoomSettings roomSettings;

    public ClientMenuActivatorComponentData(ActiveData activeData,
                                            ClientMenuActivatorComponent activatorComponent)
    {
        super(activeData, activatorComponent);

        this.roomSettings = new RoomSettings();
        this.roomSettings.init(BrainOutClient.ClientController.getUserProfile(), true);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean activate(PlayerData playerData)
    {
        if (!test(playerData))
            return false;

        Menu menu = openMenu(getContentComponent().getMenu());

        if (menu == null)
            return false;

        BrainOutClient.getInstance().topState().pushMenu(menu);

        return true;
    }

    @Override
    public boolean test(PlayerData playerData)
    {
        String menu = getContentComponent().getMenu();

        switch (menu)
        {
            case "store":
            {
                return BrainOutClient.Env.storeEnabled();
            }
            case "rsmarket":
            {
                return BrainOutClient.ClientController.getUserProfile().hasItem(
                    BrainOut.ContentMgr.get("market-pass", Achievement.class));
            }
            case "clan":
            {
                UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();
                return userProfile.isParticipatingClan();
            }
            case "workbench":
            {
                if (BrainOutClient.getInstance().topState().topMenu() instanceof LobbyMenu)
                {
                    return false;
                }

                break;
            }
        }

        return super.test(playerData);
    }

    private Menu openMenu(String menu)
    {
        switch (menu)
        {
            case "workbench":
            {
                final CSGame gameController = BrainOutClient.ClientController.getState(CSGame.class);

                if (gameController == null)
                    return null;

                return new LobbyMenu(gameController.getShopCart());
            }
            case "freeplay":
            {
                return new FreePlayQuestsMenu();
            }
            case "clan":
            {
                UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();
                return new ClanMenu(userProfile.getClanId());
            }
            case "top100players":
            {
                return new Top100Menu();
            }
            case "rsmarket":
            {
                return new MarketMenu("rs");
            }
            case "crafting":
            {
                return new MarketCraftingMenu(
                    BrainOutClient.ClientController.getState(CSGame.class).getPlayerData());
            }
            case "quickplay":
            {
                return newQuickPlayMenu();
            }
            case "store":
            {
                if (BrainOutClient.Env.storeEnabled())
                {
                    return new StoreMenu();
                }
                break;
            }
            case "findgame":
            {
                return new ServerBrowserMenu(new ServerBrowserMenu.Callback()
                {
                    @Override
                    public void selected(GameService.Room room)
                    {
                        Matchmaking.JoinRoom(room);
                    }

                    @Override
                    public void cancelled()
                    {
                        //
                    }

                    @Override
                    public void newOne()
                    {
                        BrainOutClient.getInstance().topState().pushMenu(newQuickPlayMenu());
                    }
                }, roomSettings, ServerBrowserMenu.Mode.standard);
            }
        }

        return null;
    }

    private Menu newQuickPlayMenu()
    {
        return new QuickPlayOptionsMenu(new QuickPlayOptionsMenu.Callback()
        {
            @Override
            public void selected(String name, RoomSettings settings, QuickPlayOptionsMenu menu)
            {
                find(name, settings, new Matchmaking.FindGameResult()
                {
                    @Override
                    public void success(String roomId)
                    {
                        BrainOutClient.Env.setCurrentRoom(roomId);
                    }

                    @Override
                    public void failed(Request.Result status, Request request)
                    {
                        //
                    }

                    @Override
                    public void connectionFailed()
                    {

                    }
                }, name.equals("main") || name.equals("custom"));
            }

            private void find(String name, RoomSettings settings, Matchmaking.FindGameResult result,
                              boolean trackStarted)
            {
                BrainOutClient.ClientController.setState(new CSQuickPlay(name, settings, result, trackStarted));
            }

            @Override
            public void cancelled()
            {
                //
            }
        }, roomSettings);
    }
}
