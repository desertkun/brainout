package com.desertkun.brainout.client.states;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;

import java.util.ArrayList;
import java.util.List;

public class CSGetRegions extends ControllerState
{
    @Override
    public ID getID()
    {
        return ID.getRegions;
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

        gameService.getRegions(loginService.getCurrentAccessToken(), new GameService.ListRegionsCallback()
        {
            @Override
            public void result(GameService service, Request request, Request.Result result,
                               List<GameService.Region> regions, String myRegion)
            {
                if (result == Request.Result.success)
                {
                    BrainOutClient.ClientController.setRegions(regions);
                    BrainOutClient.ClientController.setMyRegion(myRegion);

                    proceed();
                }
                else
                {
                    switchTo(new CSError(
                        L.get("MENU_ONLINE_ERROR", result.toString())
                    ));
                }
            }
        });
    }

    private void proceed()
    {
        switchTo(new CSGetBlogUpdates());
    }

    @Override
    public void release()
    {

    }
}
