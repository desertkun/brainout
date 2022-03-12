package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.gs.LoadingState;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

public class CSFindLobby extends ControllerStateWithRetry
{
    @Override
    public ID getID()
    {
        return ID.findLobby;
    }

    public CSFindLobby()
    {
        super(3);
    }

    @Override
    public void init()
    {
        Gdx.app.postRunnable(() ->
        {
            if (BrainOutClient.getInstance().topState() instanceof LoadingState)
                return;

            BrainOutClient.getInstance().switchState(new LoadingState());
        });

        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        if (gameService == null)
            throw new RuntimeException("No game service!");

        GameService.RoomsFilter filter = new GameService.RoomsFilter();
        GameService.RoomSettings settings = new GameService.RoomSettings();

        gameService.joinGame(loginService.getCurrentAccessToken(), "lobby", filter, true, settings,
            new GameService.JoinGameCallback()
        {
            @Override
            public void success(String roomId, String key, String host, int[] ports, JSONObject settings)
            {
                connect(roomId, key, host, ports, settings);
            }

            @Override
            public void fail(Request request, Request.Result status)
            {
                switch (status)
                {
                    case banned:
                    {
                        String reason = request.getResponseHeaders().getFirst("X-Ban-Reason");
                        String expires = request.getResponseHeaders().getFirst("X-Ban-Until");
                        String id = request.getResponseHeaders().getFirst("X-Ban-Id");


                        switchTo(new CSError(
                            L.get("MENU_BANNED", reason, expires, id)
                        ));
                        break;
                    }
                    case serviceUnavailable:
                    {
                        switchTo(new CSMaintenance());

                        break;
                    }
                    case gone:
                    {
                        switchTo(new CSGameOutdated());

                        break;
                    }
                    default:
                    {
                        switchTo(new CSError(
                                L.get("MENU_ONLINE_ERROR", status.toString())
                        ));
                    }
                }
            }
        });
    }

    private void connect(String roomId, String key, String host, int[] ports, JSONObject settings)
    {
        if (ports.length < 1)
        {
            throw new RuntimeException("Error, ports amount is less than 1!");
        }

        int tcp = ports[0];

        Gdx.app.postRunnable(() ->
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

            BrainOutClient.ClientController.connect(host, tcp, udp, http, key, false, -1, this::retry);
        });
    }

    @Override
    protected void retryFailed()
    {
        switchTo(new CSMaintenance());
    }

    @Override
    public void release()
    {

    }
}
