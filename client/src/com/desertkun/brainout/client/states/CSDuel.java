package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.gs.LoadingState;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.RoomSettings;
import org.anthillplatform.runtime.requests.Request;

public class CSDuel extends ControllerStateWithRetry
{
    private WaitLoadingMenu loadingMenu;

    public CSDuel()
    {
        super(3);
    }

    @Override
    protected void retryFailed()
    {
        switchTo(new CSError(L.get("MENU_FAILED_TO_CONNECT"), () -> switchTo(new CSGetRegions())));
    }

    @Override
    public ID getID()
    {
        return ID.duel;
    }

    @Override
    public void init()
    {
        RoomSettings roomSettings = new RoomSettings();
        roomSettings.setRegion(BrainOutClient.ClientController.getMyRegion());
        roomSettings.init(BrainOutClient.ClientController.getUserProfile(), false);

        loadingMenu = new WaitLoadingMenu("");
        BrainOutClient.getInstance().topState().pushMenu(loadingMenu);

        Matchmaking.FindGame("duel", roomSettings, new Matchmaking.FindGameResult()
        {
            @Override
            public void success(String roomId)
            {
            }

            @Override
            public void failed(Request.Result status, Request request)
            {
                Gdx.app.postRunnable(() ->
                {
                    BrainOutClient.getInstance().switchState(new LoadingState());

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
                            retry();
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
        }, false);
    }

    @Override
    public void release()
    {
        if (loadingMenu != null)
        {
            loadingMenu.pop();
            loadingMenu = null;
        }
    }
}
