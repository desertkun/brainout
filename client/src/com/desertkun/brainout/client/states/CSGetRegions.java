package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.esotericsoftware.minlog.Log;
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

        if (Log.INFO) Log.info("Requesting regions");

        gameService.getRegions(loginService.getCurrentAccessToken(),
            (service, request, result, regions, myRegion) -> Gdx.app.postRunnable(() ->
        {
            if (result == Request.Result.success)
            {
                if (Log.INFO) Log.info("Got regions");
                BrainOutClient.ClientController.setRegions(regions);
                BrainOutClient.ClientController.setMyRegion(myRegion);

                proceed();
            }
            else
            {
                if (Log.INFO) Log.info("Failed to get regions");
                switchTo(new CSError(
                    L.get("MENU_ONLINE_ERROR", result.toString())
                ));
            }
        }));
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
