package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.gs.LoadingState;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.RoomSettings;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.requests.Request;

public class CSQuickPlay extends ControllerStateWithRetry
{
    private final String name;
    private final RoomSettings settings;
    private final Matchmaking.FindGameResult result;
    private final boolean trackStarted;
    private WaitLoadingMenu loading;

    public CSQuickPlay(String name, RoomSettings settings, Matchmaking.FindGameResult result, boolean trackStarted)
    {
        super(3);

        this.name = name;
        this.settings = settings;
        this.result = result;
        this.trackStarted = trackStarted;
    }

    @Override
    protected void retryFailed()
    {

    }

    @Override
    public ID getID()
    {
        return ID.quickPlay;
    }

    @Override
    public void init()
    {
        loading = new WaitLoadingMenu( L.get("MENU_QUICK_PLAY"), true);

        BrainOutClient.getInstance().topState().pushMenu(loading);

        Matchmaking.FindGame(name, settings, new Matchmaking.FindGameResult()
        {
            @Override
            public void success(String roomId)
            {
                Gdx.app.postRunnable(() ->
                {
                    BrainOutClient.getInstance().switchState(new LoadingState());
                    result.success(roomId);
                });
            }

            @Override
            public void failed(Request.Result status, Request request)
            {
                Gdx.app.postRunnable(() ->
                {
                    BrainOutClient.getInstance().switchState(new LoadingState());
                    result.failed(status, request);

                    switch (status)
                    {
                        case banned:
                        {
                            String reason = request.getResponseHeaders().getFirst("X-Ban-Reason");
                            String expires = request.getResponseHeaders().getFirst("X-Ban-Until");
                            String id = request.getResponseHeaders().getFirst("X-Ban-Id");

                            BrainOutClient.ClientController.setState(new CSError(
                                    (L.get("MENU_BANNED", reason, expires, id))));

                            break;
                        }
                        case serviceUnavailable:
                        {
                            BrainOutClient.ClientController.setState(new CSMaintenance());

                            break;
                        }
                        default:
                        {
                            BrainOutClient.ClientController.setState(new CSError((
                                    L.get("MENU_CONNECTION_ERROR"))));
                            break;
                        }
                    }
                });
            }

            @Override
            public void connectionFailed()
            {
                retry();
            }
        }, trackStarted);
    }

    @Override
    public void release()
    {
        if (loading != null)
        {
            loading.pop();
            loading = null;
        }
    }
}
