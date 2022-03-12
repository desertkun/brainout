package com.desertkun.brainout.online;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.FreePlayPartnerLobby;
import com.desertkun.brainout.menu.impl.MaintenanceMenu;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

public class Matchmaking
{
    public interface FindGameResult
    {
        void success(String roomId);
        void failed(Request.Result status, Request request);
        void connectionFailed();
    }

    public interface JoinGameResult
    {
        void complete(String roomId);
        void failed(Request.Result status, Request request);
        void connectionFailed();
    }

    public static void ListGames(String name, RoomSettings filter, GameService.ListGamesCallback callback)
    {
        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        if (gameService == null)
            throw new RuntimeException("No game service!");

        GameService.RoomsFilter filterData = new GameService.RoomsFilter();
        filter.write(filterData);

        gameService.listGames(loginService.getCurrentAccessToken(),
            name, filterData, callback,
            false, filter.isShowFull(), filter.getRegion());
    }

    public static void JoinFreePlay(String partyId)
    {
        GameState topState = BrainOutClient.getInstance().topState();

        if (topState == null)
            return;

        Menu topMenu = topState.topMenu();

        if (topMenu instanceof FreePlayPartnerLobby)
        {
            topMenu.pop();
        }

        topState.pushMenu(new FreePlayPartnerLobby(partyId));
    }

    public static void JoinRoom(GameService.Room room)
    {
        WaitLoadingMenu loading = new WaitLoadingMenu(
                L.get("MENU_QUICK_PLAY"), true);

        pushMenu(loading);

        Matchmaking.JoinGame(room.id, new Matchmaking.JoinGameResult()
        {
            @Override
            public void complete(String roomId)
            {
                BrainOutClient.Env.gameStarted(roomId);

                loading.pop();
            }

            @Override
            public void failed(Request.Result status, Request request)
            {
                loading.pop();

                switch (status)
                {
                    case banned:
                    {
                        String reason = request.getResponseHeaders().getFirst("X-Ban-Reason");
                        String expires = request.getResponseHeaders().getFirst("X-Ban-Until");
                        String id = request.getResponseHeaders().getFirst("X-Ban-Id");

                        pushMenu(new AlertPopup(L.get("MENU_BANNED", reason,
                                expires,
                                id)));

                        break;
                    }
                    case gone:
                    {
                        pushMenu(new MaintenanceMenu(L.get("MENU_GAME_OUTDATED")));

                        break;
                    }
                    case serviceUnavailable:
                    {
                        pushMenu(new MaintenanceMenu(L.get("MENU_MAINTENANCE")));

                        break;
                    }
                    default:
                    {
                        pushMenu(new AlertPopup(L.get("MENU_CONNECTION_ERROR")));
                        break;
                    }
                }
            }

            @Override
            public void connectionFailed()
            {
                pushMenu(new AlertPopup(L.get("MENU_CONNECTION_ERROR")));
            }
        });
    }

    private static void pushMenu(Menu menu)
    {
        BrainOutClient.getInstance().topState().pushMenu(menu);
    }

    public static void JoinGame(String roomId, JoinGameResult callback)
    {
        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        if (gameService == null)
            throw new RuntimeException("No game service!");

        gameService.joinGame(loginService.getCurrentAccessToken(), roomId,
            new GameService.JoinGameCallback()
        {
            @Override
            public void success(String roomId, String key, String host, int[] ports, JSONObject settings)
            {
                Gdx.app.postRunnable(() ->
                {
                    callback.complete(roomId);

                    BrainOutClient.Env.gameStarted(roomId);

                    Connect(key, host, ports, settings, callback::connectionFailed);
                });
            }

            @Override
            public void fail(Request request, Request.Result status)
            {
                Gdx.app.postRunnable(() ->
                {
                    callback.failed(status, request);
                });
            }
        });
    }

    public static void FindGame(RoomSettings roomSettings, FindGameResult callback,
                                boolean trackStarted)
    {
        FindGame("main", roomSettings, callback, trackStarted);
    }

    public static void FindGame(String name, RoomSettings roomSettings, FindGameResult callback,
                                boolean trackStarted)
    {
        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        if (gameService == null)
            throw new RuntimeException("No game service!");

        GameService.RoomsFilter findFilter = new GameService.RoomsFilter();
        GameService.RoomSettings createSettings = new GameService.RoomSettings();

        roomSettings.write(findFilter);
        roomSettings.write(createSettings);

        gameService.joinGame(loginService.getCurrentAccessToken(), name, findFilter, true,
            createSettings,
            new GameService.JoinGameCallback()
        {
            @Override
            public void success(String roomId, String key, String host, int[] ports, JSONObject settings)
            {
                Gdx.app.postRunnable(() ->
                {
                    callback.success(roomId);

                    if (trackStarted)
                    {
                        BrainOutClient.Env.gameStarted(roomId);
                    }

                    Connect(key, host, ports, settings, callback::connectionFailed);
                });
            }

            @Override
            public void fail(Request request, Request.Result status)
            {
                Gdx.app.postRunnable(() ->
                {
                    callback.failed(status, request);
                });
            }
        }, false, roomSettings.getRegion());
    }

    public static void Connect(String key, String host, int[] ports, JSONObject settings,
                               Runnable onConnectionFailed)
    {
        Connect(key, host, ports, settings, null, onConnectionFailed);
    }

    public static void Connect(String key, String host, int[] ports, JSONObject settings, String partyId,
                               Runnable onConnectionFailed)
    {
        if (ports.length < 1)
        {
            throw new RuntimeException("Error, ports amount is less than 1!");
        }

        int tcp = ports[0];

        BrainOutClient.ClientController.disconnect(DisconnectReason.leave, () ->
        {
            int udp = -1;
            int http = -1;

            if (ports.length >= 2)
            {
                udp = ports[1];
            }

            if (ports.length >= 3)
            {
                http = ports[2];
            }

            BrainOutClient.ClientController.connect(host, tcp, udp, http, key, false, -1, onConnectionFailed);
        });
    }
}
