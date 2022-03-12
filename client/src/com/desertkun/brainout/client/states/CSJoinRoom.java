package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.online.Matchmaking;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

public class CSJoinRoom extends ControllerState
{
    private final String roomId;

    public CSJoinRoom(String roomId)
    {
        this.roomId = roomId;
    }

    @Override
    public ID getID()
    {
        return ID.joinRoom;
    }

    @Override
    public void init()
    {

        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null)
        {
            switchTo(new CSError("No login service!"));
            return;
        }

        if (gameService == null)
        {
            switchTo(new CSError("No game service!"));
            return;
        }

        gameService.joinGame(loginService.getCurrentAccessToken(), roomId, new GameService.JoinGameCallback()
        {
            @Override
            public void success(String roomId, String key, String host, int[] ports, JSONObject settings)
            {
                Gdx.app.postRunnable(() ->
                {
                    BrainOutClient.Env.gameStarted(roomId);
                    Matchmaking.Connect(key, host, ports, settings, CSJoinRoom.this::failedToConnect);
                });
            }

            @Override
            public void fail(Request request, Request.Result status)
            {
                switchTo(new CSError(L.get("MENU_FAILED_TO_CONNECT"),
                    () -> switchTo(new CSGetRegions())));
            }
        });
    }

    private void failedToConnect()
    {
        switchTo(new CSError(L.get("MENU_FAILED_TO_CONNECT"), () -> switchTo(new CSGetRegions())));
    }

    @Override
    public void release()
    {

    }
}
